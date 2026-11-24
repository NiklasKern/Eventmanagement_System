package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.EventmanagementApplicationRunner;
import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.dto.EventUpdateResultDto;
import at.fhv.eventmangement.application.services.NotificationService;
import at.fhv.eventmangement.domain.exceptions.EventAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.EventCategoryNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventDateHasBookingsException;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventOrganizerNotFoundException;
import at.fhv.eventmangement.domain.exceptions.MaxParticipantsBelowBookedPlacesException;
import at.fhv.eventmangement.application.form.EventDateForm;
import at.fhv.eventmangement.application.mappers.EventMapper;
import at.fhv.eventmangement.domain.model.*;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.domain.repositories.EventCategoryRepository;
import at.fhv.eventmangement.domain.repositories.EventOrganizerRepository;
import at.fhv.eventmangement.domain.repositories.EventRepository;
import at.fhv.eventmangement.application.services.EventService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import at.fhv.eventmangement.domain.exceptions.EventAlreadyCancelledException;

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

    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository,
                            EventCategoryRepository eventCategoryRepository,
                            EventOrganizerRepository eventOrganizerRepository,
                            EventUpdateTransaction eventUpdateTransaction,
                            EventCancelTransaction eventCancelTransaction,
                            NotificationService notificationService,
                            EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventCategoryRepository = eventCategoryRepository;
        this.eventOrganizerRepository = eventOrganizerRepository;
        this.eventUpdateTransaction = eventUpdateTransaction;
        this.eventCancelTransaction = eventCancelTransaction;
        this.notificationService = notificationService;
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
    public EventUpdateResultDto updateEvent(UUID id, EventForm form)
            throws EventNotFoundException, EventAlreadyExistsException, EventCategoryNotFoundException,
            EventDateHasBookingsException, MaxParticipantsBelowBookedPlacesException {

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
    public EventDto addEvent(EventForm eventForm)
            throws EventAlreadyExistsException, EventCategoryNotFoundException, EventOrganizerNotFoundException {

        if(eventRepository.existsByName(eventForm.getName())) {
            throw new EventAlreadyExistsException();
        }

        EventCategory category = eventCategoryRepository
                .findById(eventForm.getEventCategoryId())
                .orElseThrow(EventCategoryNotFoundException::new);

        EventOrganizer organizer = eventOrganizerRepository.findByContactInfoName(EventmanagementApplicationRunner.DEFAULT_ORGANIZER)
                .orElseThrow(EventOrganizerNotFoundException::new);

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

        for(EventDateForm eventDateForm : eventForm.getEventDates())
        {
            event.getEventDates().add(EventDate.builder()
                    .event(event)
                    .startTime(eventDateForm.getStartTime())
                    .endTime(eventDateForm.getEndTime())
                    .build());
        }


        //Save event to db and return saved eventdto
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
}
