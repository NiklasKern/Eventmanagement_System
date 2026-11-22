package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.dto.EventUpdateResultDto;
import at.fhv.eventmangement.application.form.EventDateForm;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.application.form.EventSearchForm;
import at.fhv.eventmangement.application.mappers.EventMapper;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.application.services.NotificationService;
import at.fhv.eventmangement.domain.exceptions.*;
import at.fhv.eventmangement.domain.model.*;
import at.fhv.eventmangement.domain.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import at.fhv.eventmangement.domain.exceptions.EventAlreadyCancelledException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Validated
@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final EventOrganizerRepository eventOrganizerRepository;
    private final EventUpdateTransaction eventUpdateTransaction;
    private final EventCancelTransaction eventCancelTransaction;
    private final NotificationService notificationService;
    private final UserAccountRepository userAccountRepository;

    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository,
                            EventCategoryRepository eventCategoryRepository,
                            EventOrganizerRepository eventOrganizerRepository,
                            EventUpdateTransaction eventUpdateTransaction,
                            EventCancelTransaction eventCancelTransaction,
                            NotificationService notificationService,
                            UserAccountRepository userAccountRepository,
                            EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventCategoryRepository = eventCategoryRepository;
        this.eventOrganizerRepository = eventOrganizerRepository;
        this.eventUpdateTransaction = eventUpdateTransaction;
        this.eventCancelTransaction = eventCancelTransaction;
        this.notificationService = notificationService;
        this.userAccountRepository = userAccountRepository;
        this.eventMapper = eventMapper;
    }

    @Override
    public EventDto getEventById(UUID id) throws EventNotFoundException {
        return eventMapper.toDto(eventRepository.findById(id)
                .orElseThrow(EventNotFoundException::new));
    }

    // Runs without a surrounding transaction: the changes are committed by EventUpdateTransaction first,
    // only afterwards the customers are notified. Only successfully notified customers are returned.
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public EventUpdateResultDto updateEvent(UUID id, EventForm form, String username)
            throws EventNotFoundException, EventAlreadyExistsException, EventCategoryNotFoundException,
            EventDateHasBookingsException, MaxParticipantsBelowBookedPlacesException, NotEventOwnerException {

        // Check first, before anything is changed
        editableEvent(id, username);

        EventUpdateTransaction.Result result = eventUpdateTransaction.apply(id, form);
        Event saved = result.event();

        List<String> notified = result.customersToNotify().stream()
                .filter(customer -> notificationService.notifyEventChanged(customer, saved))
                .map(customer -> customer.getContactInfo().getName())
                .toList();

        return new EventUpdateResultDto(eventMapper.toDto(saved), notified);
    }

    @Override
    public List<EventDto> getActiveEvents() {
        return eventRepository.findByEventStatus(EventStatus.ACTIVE).stream().map(eventMapper::toDto).toList();
    }

    @Override
    public List<EventDto> getAllEvents() {
        return eventRepository.findAll().stream().map(eventMapper::toDto).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventDto addEvent(EventForm eventForm, String username)
            throws EventAlreadyExistsException, EventCategoryNotFoundException, EventOrganizerNotFoundException {

        if (eventRepository.existsByName(eventForm.getName())) {
            throw new EventAlreadyExistsException();
        }

        EventCategory category = eventCategoryRepository
                .findById(eventForm.getEventCategoryId())
                .orElseThrow(EventCategoryNotFoundException::new);

        EventOrganizer organizer = organizerFor(account(username), eventForm.getEventOrganizerId());

        //Create new event to prevent unexpected input
        Event event = Event.builder()
                .name(eventForm.getName())
                .description(Optional.ofNullable(eventForm.getDescription()).orElse(""))
                .location(eventForm.getLocation())
                .price(eventForm.getPrice())
                .minParticipants(eventForm.getMinParticipants())
                .maxParticipants(eventForm.getMaxParticipants())
                .eventStatus(EventStatus.ACTIVE)
                .eventCategory(category)
                .eventOrganizer(organizer)
                .build();

        for (EventDateForm eventDateForm : eventForm.getEventDates()) {
            event.getEventDates().add(EventDate.builder()
                    .event(event)
                    .startTime(eventDateForm.getStartTime())
                    .endTime(eventDateForm.getEndTime())
                    .build());
        }

        // Save event to db and return saved eventdto
        return eventMapper.toDto(eventRepository.save(event));
    }
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public EventUpdateResultDto cancelEvent(UUID id)
            throws EventNotFoundException, EventAlreadyCancelledException {

        // excute cancellation and save to db
        EventCancelTransaction.Result result = eventCancelTransaction.apply(id);
        Event saved = result.event();

        // send event cancellation notification to customers and gather the names of those who were successfully notified
        List<String> notified = result.customersToNotify().stream()
                .filter(customer -> notificationService.notifyEventCancelled(customer, saved))
                .map(customer -> customer.getContactInfo().getName())
                .toList();

        // 3.return rsult to controller
        return new EventUpdateResultDto(eventMapper.toDto(saved), notified);
    }

    @Override
    public List<EventDto> searchEvents(EventSearchForm searchForm) {
        return eventRepository.search(toCriteria(searchForm)).stream().map(eventMapper::toDto).toList();
    }

    // Organizer list: everything with EDIT_ALL_EVENTS, otherwise only the events of the own organizer
    @Override
    public List<EventDto> getManagedEvents(String username) {
        UserAccount account = account(username);
        if (account.hasPermission(Permissions.EDIT_ALL_EVENTS)) {
            return getAllEvents();
        }
        if (account.getOrganizer() == null) {
            return List.of();
        }
        return eventRepository.findByEventOrganizerId(account.getOrganizer().getId()).stream()
                .map(eventMapper::toDto)
                .toList();
    }

    // Edit form: only open it if the user may save it afterwards
    @Override
    public EventDto getEventForEdit(UUID id, String username) throws EventNotFoundException, NotEventOwnerException {
        return eventMapper.toDto(editableEvent(id, username));
    }

    @Override
    public List<String> getEventLocation() {
        return eventRepository.findLocations(EventStatus.ACTIVE);
    }

    private static EventSearchCriteria toCriteria(EventSearchForm searchForm) {
        LocalDate firstDay = searchForm.getDateFrom();
        LocalDate lastDay = searchForm.getDateTo() != null ? searchForm.getDateTo() : searchForm.getDateFrom();

        return EventSearchCriteria.builder()
                .status(EventStatus.ACTIVE)
                .text(blankToNull(searchForm.getText()))
                .location(blankToNull(searchForm.getLocation()))
                .categoryId(searchForm.getCategoryId())
                .from(firstDay == null ? null : firstDay.atStartOfDay())
                .to(lastDay == null ? null : lastDay.plusDays(1).atStartOfDay())
                .priceFrom(searchForm.getPriceFrom())
                .priceTo(searchForm.getPriceTo())
                .build();
    }

    // The logged-in user always has an account. If not, that is a bug, not a user error → unchecked
    private UserAccount account(String username) {
        return userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("No account for logged-in user " + username));
    }

    // Loads the event and checks that the user may change it
    private Event editableEvent(UUID id, String username) throws EventNotFoundException, NotEventOwnerException {
        Event event = eventRepository.findById(id).orElseThrow(EventNotFoundException::new);
        if (!account(username).canEdit(event)) {
            throw new NotEventOwnerException();
        }
        return event;
    }

    // Providers always create for their own organizer.
    // Users with EDIT_ALL_EVENTS (Event Management, Admin) may choose one in the form.
    private EventOrganizer organizerFor(UserAccount account, UUID chosenOrganizerId) throws EventOrganizerNotFoundException {
        if (chosenOrganizerId != null && account.hasPermission(Permissions.EDIT_ALL_EVENTS)) {
            return eventOrganizerRepository.findById(chosenOrganizerId)
                    .orElseThrow(EventOrganizerNotFoundException::new);
        }
        if (account.getOrganizer() == null) {
            throw new EventOrganizerNotFoundException("Your account is not linked to an event organizer, please choose one");
        }
        return account.getOrganizer();
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}