package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.EventmanagementApplicationRunner;
import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.dto.EventUpdateResultDto;
import at.fhv.eventmangement.application.form.EventDateForm;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.application.mappers.EventMapper;
import at.fhv.eventmangement.application.services.NotificationService;
import at.fhv.eventmangement.domain.exceptions.*;
import at.fhv.eventmangement.domain.model.*;
import at.fhv.eventmangement.domain.repositories.EventCategoryRepository;
import at.fhv.eventmangement.domain.repositories.EventOrganizerRepository;
import at.fhv.eventmangement.domain.repositories.EventRepository;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    private static final String USERNAME = "provider";

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventCategoryRepository eventCategoryRepository;
    @Mock
    private EventOrganizerRepository eventOrganizerRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private EventMapper eventMapper;
    @Mock
    private EventUpdateTransaction eventUpdateTransaction;
    @Mock
    private EventCancelTransaction eventCancelTransaction;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EventServiceImpl eventService;

    private EventCategory category;
    private EventOrganizer organizer;

    @BeforeEach
    void setUp() {
        category = new EventCategory("Hiking");
        // needs an id: canEdit compares organizer ids
        organizer = EventOrganizer.builder()
                .id(UUID.randomUUID())
                .contactInfo(new ContactInfo(EventmanagementApplicationRunner.DEFAULT_ORGANIZER, "Street 1", "org@test.com"))
                .build();
    }

    private EventForm validForm() {
        return EventForm.builder()
                .name("Summer Hike")
                .description("A nice walk")
                .location("Dornbirn")
                .eventDates(List.of(EventDateForm.builder()
                        .startTime(LocalDateTime.now().plusDays(10))
                        .endTime(LocalDateTime.now().plusDays(10).plusHours(3))
                        .build()
                ))
                .price(new BigDecimal("25.00"))
                .minParticipants(3)
                .maxParticipants(20)
                .eventCategoryId(category.getId())
                .build();
    }

    // The account of the logged-in user "provider", linked to the given organizer (or none)
    private void loggedIn(EventOrganizer linkedOrganizer, Permissions... permissions) {
        Role role = Role.builder().name("Test").permissions(EnumSet.of(permissions[0], permissions)).build();
        when(userAccountRepository.findByUsername(USERNAME)).thenReturn(Optional.of(UserAccount.builder()
                .username(USERNAME)
                .enabled(true)
                .role(role)
                .organizer(linkedOrganizer)
                .build()));
    }

    // getEvents

    @Test
    void getAllEvents_mapsEveryEventFromRepository() throws Exception {
        Event first = Event.builder().name("A").build();
        Event second = Event.builder().name("B").build();
        EventDto firstDto = mock(EventDto.class);
        EventDto secondDto = mock(EventDto.class);
        when(eventRepository.findAll()).thenReturn(List.of(first, second));
        when(eventMapper.toDto(first)).thenReturn(firstDto);
        when(eventMapper.toDto(second)).thenReturn(secondDto);

        List<EventDto> result = eventService.getAllEvents();

        assertThat(result).containsExactly(firstDto, secondDto);
    }

    @Test
    void getEvents_returnsEmptyListWhenNoAllEvents() throws Exception {
        when(eventRepository.findAll()).thenReturn(List.of());

        assertThat(eventService.getAllEvents()).isEmpty();
    }

    // getManagedEvents

    @Test
    void getManagedEvents_provider_seesOnlyOwnEvents() {
        Event own = Event.builder().name("Own").build();
        EventDto ownDto = mock(EventDto.class);
        loggedIn(organizer, Permissions.EDIT_OWN_EVENTS);
        when(eventRepository.findByEventOrganizerId(organizer.getId())).thenReturn(List.of(own));
        when(eventMapper.toDto(own)).thenReturn(ownDto);

        assertThat(eventService.getManagedEvents(USERNAME)).containsExactly(ownDto);
        verify(eventRepository, never()).findAll();
    }

    @Test
    void getManagedEvents_editAll_seesEverything() {
        loggedIn(null, Permissions.EDIT_ALL_EVENTS);
        when(eventRepository.findAll()).thenReturn(List.of());

        assertThat(eventService.getManagedEvents(USERNAME)).isEmpty();
        verify(eventRepository).findAll();
    }

    @Test
    void getManagedEvents_accountWithoutOrganizer_seesNothing() {
        loggedIn(null, Permissions.EVENT_CREATE);

        assertThat(eventService.getManagedEvents(USERNAME)).isEmpty();
        verify(eventRepository, never()).findAll();
    }

    // addEvent

    @Test
    void addEvent_savesEventForOrganizerOfLoggedInAccount() throws Exception {
        EventForm form = validForm();
        when(eventRepository.existsByName(form.getName())).thenReturn(false);
        when(eventCategoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        loggedIn(organizer, Permissions.EVENT_CREATE);
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        eventService.addEvent(form, USERNAME);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        Event saved = captor.getValue();
        assertThat(saved.getId()).as("id is generated by the database").isNull();
        assertThat(saved.getName()).isEqualTo("Summer Hike");
        assertThat(saved.getDescription()).isEqualTo("A nice walk");
        assertThat(saved.getLocation()).isEqualTo("Dornbirn");
        assertThat(saved.getPrice()).isEqualByComparingTo("25.00");
        assertThat(saved.getMinParticipants()).isEqualTo(3);
        assertThat(saved.getMaxParticipants()).isEqualTo(20);
        assertThat(saved.getEventDates()).singleElement().satisfies(date -> {
            assertThat(date.getStartTime()).isEqualTo(form.getEventDates().getFirst().getStartTime());
            assertThat(date.getEndTime()).isEqualTo(form.getEventDates().getFirst().getEndTime());
            assertThat(date.getEvent()).isSameAs(saved);
        });
        assertThat(saved.getEventStatus()).isEqualTo(EventStatus.ACTIVE);
        assertThat(saved.getEventCategory()).isSameAs(category);
        assertThat(saved.getEventOrganizer()).isSameAs(organizer);
    }

    @Test
    void addEvent_returnsDtoOfTheSavedEventNotTheUnsavedOne() throws Exception {
        EventForm form = validForm();
        Event savedWithId = Event.builder().id(UUID.randomUUID()).name(form.getName()).build();
        EventDto expected = mock(EventDto.class);
        when(eventCategoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        loggedIn(organizer, Permissions.EVENT_CREATE);
        when(eventRepository.save(any(Event.class))).thenReturn(savedWithId);
        when(eventMapper.toDto(savedWithId)).thenReturn(expected);

        EventDto result = eventService.addEvent(form, USERNAME);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void addEvent_storesEmptyStringWhenDescriptionIsMissing() throws Exception {
        EventForm form = validForm();
        form.setDescription(null);
        when(eventCategoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        loggedIn(organizer, Permissions.EVENT_CREATE);
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        eventService.addEvent(form, USERNAME);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        assertThat(captor.getValue().getDescription()).isEmpty();
    }

    @Test
    void addEvent_acceptsMinEqualToMax() throws Exception {
        EventForm form = validForm();
        form.setMinParticipants(10);
        form.setMaxParticipants(10);
        when(eventCategoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        loggedIn(organizer, Permissions.EVENT_CREATE);
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        eventService.addEvent(form, USERNAME);

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void addEvent_editAllEvents_mayChooseOrganizer() throws Exception {
        EventOrganizer chosen = EventOrganizer.builder().id(UUID.randomUUID()).build();
        EventForm form = validForm();
        form.setEventOrganizerId(chosen.getId());
        when(eventCategoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(eventOrganizerRepository.findById(chosen.getId())).thenReturn(Optional.of(chosen));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        loggedIn(null, Permissions.EVENT_CREATE, Permissions.EDIT_ALL_EVENTS);

        eventService.addEvent(form, USERNAME);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        assertThat(captor.getValue().getEventOrganizer()).isSameAs(chosen);
    }

    @Test
    void addEvent_provider_cannotChooseAnotherOrganizer() throws Exception {
        EventForm form = validForm();
        form.setEventOrganizerId(UUID.randomUUID());   // ignored: no EDIT_ALL_EVENTS
        when(eventCategoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        loggedIn(organizer, Permissions.EVENT_CREATE);

        eventService.addEvent(form, USERNAME);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        assertThat(captor.getValue().getEventOrganizer()).isSameAs(organizer);
        verify(eventOrganizerRepository, never()).findById(any());
    }

    // addEvent: rejected input

    // min > max is validated by EventForm (@AssertTrue), see EventFormValidationTest

    @Test
    void addEvent_rejectsDuplicateName() throws Exception {
        EventForm form = validForm();
        when(eventRepository.existsByName("Summer Hike")).thenReturn(true);

        assertThatThrownBy(() -> eventService.addEvent(form, USERNAME))
                .isInstanceOf(EventAlreadyExistsException.class)
                .hasMessage("event name already exists");
        verify(eventRepository, never()).save(any());
    }

    @Test
    void addEvent_rejectsUnknownCategory() throws Exception {
        EventForm form = validForm();
        when(eventCategoryRepository.findById(category.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.addEvent(form, USERNAME))
                .isInstanceOf(EventCategoryNotFoundException.class)
                .hasMessage("unknown category");
        verify(eventRepository, never()).save(any());
    }

    @Test
    void addEvent_accountWithoutOrganizer_isRejected() throws Exception {
        EventForm form = validForm();
        when(eventCategoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        loggedIn(null, Permissions.EVENT_CREATE);

        assertThatThrownBy(() -> eventService.addEvent(form, USERNAME))
                .isInstanceOf(EventOrganizerNotFoundException.class)
                .hasMessageContaining("not linked to an event organizer");
        verify(eventRepository, never()).save(any());
    }

    // getEventForEdit

    @Test
    void getEventForEdit_foreignEvent_isRejected() {
        UUID id = UUID.randomUUID();
        EventOrganizer other = EventOrganizer.builder().id(UUID.randomUUID()).build();
        when(eventRepository.findById(id)).thenReturn(Optional.of(Event.builder().eventOrganizer(other).build()));
        loggedIn(organizer, Permissions.EDIT_OWN_EVENTS);

        assertThatThrownBy(() -> eventService.getEventForEdit(id, USERNAME))
                .isInstanceOf(NotEventOwnerException.class);
    }

    // update event

    private static Customer customer(String name) {
        return Customer.builder()
                .id(UUID.randomUUID())
                .contactInfo(new ContactInfo(name, "Street 1", name + "@test.com"))
                .build();
    }

    @Test
    void updateEvent_onlyListsCustomersWhoseNotificationWasSent() throws Exception {
        UUID id = UUID.randomUUID();
        EventForm form = validForm();
        Event saved = Event.builder().name("Summer Hike").build();
        Customer reached = customer("Anna");
        Customer failed = customer("Bernd");
        when(eventRepository.findById(id)).thenReturn(Optional.of(Event.builder().eventOrganizer(organizer).build()));
        loggedIn(organizer, Permissions.EDIT_OWN_EVENTS);
        when(eventUpdateTransaction.apply(id, form))
                .thenReturn(new EventUpdateTransaction.Result(saved, List.of(reached, failed)));
        when(notificationService.notifyEventChanged(reached, saved)).thenReturn(true);
        when(notificationService.notifyEventChanged(failed, saved)).thenReturn(false);

        EventUpdateResultDto result = eventService.updateEvent(id, form, USERNAME);

        assertThat(result.notifiedCustomers()).containsExactly("Anna");
    }

    @Test
    void updateEvent_failingUpdateSendsNoNotification() throws Exception {
        UUID id = UUID.randomUUID();
        EventForm form = validForm();
        when(eventRepository.findById(id)).thenReturn(Optional.of(Event.builder().eventOrganizer(organizer).build()));
        loggedIn(organizer, Permissions.EDIT_OWN_EVENTS);
        when(eventUpdateTransaction.apply(id, form)).thenThrow(new EventAlreadyExistsException());

        assertThatThrownBy(() -> eventService.updateEvent(id, form, USERNAME))
                .isInstanceOf(EventAlreadyExistsException.class);
        verify(notificationService, never()).notifyEventChanged(any(), any());
    }

    @Test
    void updateEvent_foreignEvent_isRejectedBeforeAnythingChanges() throws Exception {
        UUID id = UUID.randomUUID();
        EventOrganizer other = EventOrganizer.builder().id(UUID.randomUUID()).build();
        when(eventRepository.findById(id)).thenReturn(Optional.of(Event.builder().eventOrganizer(other).build()));
        loggedIn(organizer, Permissions.EDIT_OWN_EVENTS);

        assertThatThrownBy(() -> eventService.updateEvent(id, validForm(), USERNAME))
                .isInstanceOf(NotEventOwnerException.class);
        verify(eventUpdateTransaction, never()).apply(any(), any());
    }

    // cancel event

    @Test
    void cancelEvent_onlyListsCustomersWhoseNotificationWasSent() throws Exception {
        UUID id = UUID.randomUUID();
        Event saved = Event.builder().name("Summer Hike").eventStatus(EventStatus.CANCELLED).build();
        EventDto expectedDto = mock(EventDto.class);
        Customer reached = customer("Anna");
        Customer failed = customer("Bernd");

        when(eventRepository.findById(id)).thenReturn(Optional.of(Event.builder().eventOrganizer(organizer).build()));
        loggedIn(organizer, Permissions.EDIT_OWN_EVENTS);
        when(eventCancelTransaction.apply(id))
                .thenReturn(new EventCancelTransaction.Result(saved, List.of(reached, failed)));
        when(notificationService.notifyEventCancelled(reached, saved)).thenReturn(true);
        when(notificationService.notifyEventCancelled(failed, saved)).thenReturn(false);
        when(eventMapper.toDto(saved)).thenReturn(expectedDto);

        EventUpdateResultDto result = eventService.cancelEvent(id, USERNAME);

        assertThat(result.event()).isSameAs(expectedDto);
        assertThat(result.notifiedCustomers()).containsExactly("Anna");
    }

    @Test
    void cancelEvent_foreignEvent_isRejectedBeforeAnythingChanges() throws Exception {
        UUID id = UUID.randomUUID();
        EventOrganizer other = EventOrganizer.builder().id(UUID.randomUUID()).build();
        when(eventRepository.findById(id)).thenReturn(Optional.of(Event.builder().eventOrganizer(other).build()));
        loggedIn(organizer, Permissions.EDIT_OWN_EVENTS);

        assertThatThrownBy(() -> eventService.cancelEvent(id, USERNAME))
                .isInstanceOf(NotEventOwnerException.class);
        verify(eventCancelTransaction, never()).apply(any());
        verify(notificationService, never()).notifyEventCancelled(any(), any());
    }

    @Test
    void updateEvent_editAllEvents_mayChangeForeignEvent() throws Exception {
        UUID id = UUID.randomUUID();
        EventForm form = validForm();
        Event saved = Event.builder().name("Summer Hike").build();
        EventOrganizer other = EventOrganizer.builder().id(UUID.randomUUID()).build();
        when(eventRepository.findById(id)).thenReturn(Optional.of(Event.builder().eventOrganizer(other).build()));
        loggedIn(null, Permissions.EDIT_ALL_EVENTS);
        when(eventUpdateTransaction.apply(id, form)).thenReturn(new EventUpdateTransaction.Result(saved, List.of()));

        EventUpdateResultDto result = eventService.updateEvent(id, form, USERNAME);

        assertThat(result.notifiedCustomers()).isEmpty();
        verify(eventUpdateTransaction).apply(id, form);
    }
}