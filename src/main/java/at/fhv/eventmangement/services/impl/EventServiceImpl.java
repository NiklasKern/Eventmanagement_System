package at.fhv.eventmangement.services.impl;

import at.fhv.eventmangement.EventmanagementApplicationRunner;
import at.fhv.eventmangement.domain.form.EventForm;
import at.fhv.eventmangement.domain.entities.Event;
import at.fhv.eventmangement.domain.entities.EventCategory;
import at.fhv.eventmangement.domain.entities.EventStatus;
import at.fhv.eventmangement.exceptions.ValidationException;
import at.fhv.eventmangement.repositories.EventCategoryRepository;
import at.fhv.eventmangement.repositories.EventOrganizerRepository;
import at.fhv.eventmangement.repositories.EventRepository;
import at.fhv.eventmangement.services.EventService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        if (form.getMinParticipants() > form.getMaxParticipants()) {
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
        eventRepository.save(new Event(
                null,
                form.getName(),
                Optional.ofNullable(form.getDescription()).orElse(""),
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

}
