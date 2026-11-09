package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.EventmanagementApplicationRunner;
import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.dto.EventUpdateResultDto;
import at.fhv.eventmangement.application.form.EventDateForm;
import at.fhv.eventmangement.application.mappers.EventMapper;
import at.fhv.eventmangement.application.services.NotificationService;
import at.fhv.eventmangement.domain.exceptions.NotFoundException;
import at.fhv.eventmangement.domain.model.*;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.domain.exceptions.ValidationException;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import at.fhv.eventmangement.domain.repositories.EventCategoryRepository;
import at.fhv.eventmangement.domain.repositories.EventOrganizerRepository;
import at.fhv.eventmangement.domain.repositories.EventRepository;
import at.fhv.eventmangement.application.services.EventService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

@Validated
@Service
@Transactional(readOnly = true) //add @Transactional to your method if you want it to change something
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final EventOrganizerRepository eventOrganizerRepository;
    private final EventBookingRepository eventBookingRepository;
    private final NotificationService notificationService;

    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository,
                            EventCategoryRepository eventCategoryRepository,
                            EventOrganizerRepository eventOrganizerRepository,
                            EventBookingRepository eventBookingRepository,
                            NotificationService notificationService,
                            EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventCategoryRepository = eventCategoryRepository;
        this.eventOrganizerRepository = eventOrganizerRepository;
        this.eventBookingRepository = eventBookingRepository;
        this.notificationService = notificationService;
        this.eventMapper = eventMapper;
    }

    @Override
    public EventDto getEventById(UUID id) {
        return eventMapper.toDto(eventRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("event " + id + " not found"))
        );
    }

    @Override
    @Transactional
    public EventUpdateResultDto updateEvent(UUID id, EventForm form) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("event " + id + " not found"));

        String name = form.getName().trim();
        if (!event.getName().equalsIgnoreCase(name) && eventRepository.existsByName(name)) {
            throw new ValidationException(Map.of("name", "event name already exists"));
        }

        EventCategory category = eventCategoryRepository.findById(form.getEventCategoryId())
                .orElseThrow(() -> new ValidationException(Map.of("eventCategoryId", "unknown category")));

        event.setName(name);
        event.setDescription(Optional.ofNullable(form.getDescription()).orElse(""));
        event.setLocation(form.getLocation());
        event.setPrice(form.getPrice());
        event.setMinParticipants(form.getMinParticipants());
        event.setMaxParticipants(form.getMaxParticipants());
        event.setEventCategory(category);

        Map<UUID, EventDate> existing = event.getEventDates().stream().collect(Collectors.toMap(EventDate::getId, d -> d));

        List<EventDate> updatedDates = new ArrayList<>();
        for (EventDateForm dateForm : form.getEventDates()) {
            EventDate date = dateForm.getId() == null ? null : existing.remove(dateForm.getId());
            if (date != null) {
                date.setStartTime(dateForm.getStartTime());
                date.setEndTime(dateForm.getEndTime());
                updatedDates.add(date);
            } else {
                updatedDates.add(EventDate.builder()
                        .event(event)
                        .startTime(dateForm.getStartTime())
                        .endTime(dateForm.getEndTime())
                        .build());
            }
        }

        for (EventDate removed : existing.values()) {
            if (eventBookingRepository.hasActiveBookings(removed.getId())) {
                throw new ValidationException(Map.of("eventDates", "a date with bookings cannot be removed"));
            }
        }

        event.getEventDates().clear();
        event.getEventDates().addAll(updatedDates);

        Event saved = eventRepository.save(event);

        Map<UUID, Customer> customers = new LinkedHashMap<>();
        eventBookingRepository.findActiveBookingsByEventId(id)
                .forEach(b -> customers.putIfAbsent(b.getCustomer().getId(), b.getCustomer()));
        customers.values().forEach(c -> notificationService.notifyEventChanged(c, saved));

        List<String> notified = customers.values().stream()
                .map(c -> c.getContactInfo().getName())
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
    @Transactional
    public EventDto addEvent(EventForm eventForm) {

        if (eventRepository.existsByName(eventForm.getName())) {
            throw new ValidationException(
                    Map.of("name", "event name already exists"));
        }

        EventCategory category = eventCategoryRepository
                .findById(eventForm.getEventCategoryId())
                .orElseThrow(() -> new ValidationException(
                        Map.of("eventCategoryId", "unknown category")));

        EventOrganizer organizer = eventOrganizerRepository.findByContactInfoName(EventmanagementApplicationRunner.DEFAULT_ORGANIZER).orElseThrow();

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

        //Save event to db
        Event saved = eventRepository.save(event);
        return eventMapper.toDto(saved);
    }

}
