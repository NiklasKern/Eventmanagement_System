package at.fhv.eventmangement.services.impl;

import at.fhv.eventmangement.domain.dto.EventForm;
import at.fhv.eventmangement.domain.entities.Event;
import at.fhv.eventmangement.domain.entities.EventStatus;
import at.fhv.eventmangement.exceptions.ValidationException;
import at.fhv.eventmangement.repositories.EventRepository;
import at.fhv.eventmangement.services.EventService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Validated
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository  eventCategoryRepository;
    private final EventOrganizerRepository eventOrganizerRepository;

    public EventServiceImpl(EventRepository eventRepository, EventCategoryRepository eventCategoryRepository, EventOrganizerRepository eventOrganizerRepository) {
        this.eventRepository = eventRepository;
        this.eventCategoryRepository = eventCategoryRepository;
        this.eventOrganizerRepository = eventOrganizerRepository;
    }


    @Override
    public List<Event> getEvents() {
        return eventRepository.findAll();
    }

    @Override
    public void addEvent(EventForm form) {

        if (form.getMinParticipants() < form.getMaxParticipants()) {
            throw new ValidationException(
                    Map.of("maxParticipants", "must not be less than minimum participants"));
        }

        if(eventRepository.existsByName(form.getName())) {
            throw new ValidationException(
                    Map.of("name", "event name already exists"));
        }

        EventCategory category = eventCategoryRepository
                .findById(form.getEventCategoryId())
                .orElseThrow(() -> new ValidationException(
                        Map.of("eventCategoryId", "unknown category")));

        LocalDateTime now = LocalDateTime.now();

        //Create new event to prevent unexpected input
        return eventRepository.save(new Event(
                null,
                form.getName(),
                Optional.ofNullable(form.getDescription()).orElse(""),
                form.getOrganizer(),
                form.getLocation(),
                form.getPrice(),
                form.getMinParticipants(),
                form.getMaxParticipants(),
                form.getDateTime(),
                EventStatus.OPEN,
                category,
                eventOrganizerRepository.findByOrganizerName(EventmanagementApplicationRunner.DEFAULT_ORGANIZER).orElseThrow(),
                now,
                now
        ));
    }

    // Placeholder: later load categories from DB
    @Override
    public List<String> getEventCategories() {
        return List.of("Hiking");
    }
}
