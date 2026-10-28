package at.fhv.eventmangement.services.impl;

import at.fhv.eventmangement.domain.dto.EventForm;
import at.fhv.eventmangement.domain.entities.Event;
import at.fhv.eventmangement.domain.entities.EventStatus;
import at.fhv.eventmangement.exceptions.ValidationException;
import at.fhv.eventmangement.repositories.EventCategoryRepository;
import at.fhv.eventmangement.repositories.EventRepository;
import at.fhv.eventmangement.services.EventService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository  eventCategoryRepository;

    public EventServiceImpl(EventRepository eventRepository, EventCategoryRepository eventCategoryRepository) {
        this.eventRepository = eventRepository;
        this.eventCategoryRepository = eventCategoryRepository;
    }


    @Override
    public List<Event> getEvents() {
        return eventRepository.findAll();
    }

    @Override
    public Event addEvent(EventForm form) {

        validate(form);

        LocalDateTime now = LocalDateTime.now();

        //Create new event to prevent unexpected input
        return eventRepository.save(new Event(
                null,
                form.getName(),
                Optional.ofNullable(form.getDescription()).orElse(""),
                form.getLocation(),
                form.getDateTime(),
                form.getPrice(),
                form.getCapacity(),
                EventStatus.OPEN,
                //Already validated
                eventCategoryRepository.findCategoryById(form.getEventCategoryId()).orElseThrow(),
                form.getOrganizer(),
                now,
                now
        ));
    }

    // Checks rules for new event
    // First collects all errors then throws them at the end.
    // throws ValidationException if at least one field is invalid
    private void validate(EventForm form) {
        Map<String, String> errors = new LinkedHashMap<>();

        // Mandatory
        if (isBlank(form.getName()))
            errors.put("name", "fill mandatory fields");
        if (isBlank(form.getLocation()))
            errors.put("location", "fill mandatory fields");
        // Mandatory and in the future
        if (form.getDateTime() == null)
            errors.put("dateTime", "fill mandatory fields");
        else if (!form.getDateTime().isAfter(LocalDateTime.now()))
            errors.put("dateTime", "Date has to be in the future");
        // Mandatory and > 0
        if (form.getPrice() == null || form.getPrice() <= 0)
            errors.put("price", "must be greater than 0");
        if (form.getCapacity() == null || form.getCapacity() <= 0)
            errors.put("capacity", "must be greater than 0");

        if (form.getEventCategoryId() == null)
            errors.put("eventCategoryId", "fill mandatory fields");
        else if (!eventCategoryRepository.existsById(form.getEventCategoryId()))
            errors.put("eventCategoryId", "unknown category");

        if (!errors.isEmpty())
            throw new ValidationException(errors);
    }

    // True if string is null, empty or whitespace
    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
