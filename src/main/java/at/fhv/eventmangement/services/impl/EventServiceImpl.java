package at.fhv.eventmangement.services.impl;

import at.fhv.eventmangement.domain.dto.EventForm;
import at.fhv.eventmangement.domain.entities.Event;
import at.fhv.eventmangement.domain.entities.EventStatus;
import at.fhv.eventmangement.repositories.EventRepository;
import at.fhv.eventmangement.services.EventService;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Validated
@Component
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }


    @Override
    public List<Event> getEvents() {
        return eventRepository.findAll();
    }

    @Override
    public Event addEvent(EventForm form) {

        LocalDateTime now = LocalDateTime.now();

        //Create new event to prevent unexpected input
        return eventRepository.save(new Event(
                null,
                form.getName(),
                Optional.ofNullable(form.getDescription()).orElse(""),
                form.getOrganizer(),
                form.getLocation(),
                form.getDateTime(),
                form.getPrice(),
                form.getCapacity(),
                EventStatus.OPEN,
                form.getEventCategory(),
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
