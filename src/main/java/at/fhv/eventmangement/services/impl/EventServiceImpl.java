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
                form.getEventCategory(),
                now,
                now
        ));
    }

    private void validate(EventForm form) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (isBlank(form.getName()))
            errors.put("name", "fill mandatory fields");
        if (isBlank(form.getLocation()))
            errors.put("location", "fill mandatory fields");
        if (form.getDateTime() == null)
            errors.put("dateTime", "fill mandatory fields");
        else if (!form.getDateTime().isAfter(LocalDateTime.now()))
            errors.put("dateTime", "Date has to be in the future");
        if (form.getPrice() == null || form.getPrice() <= 0)
            errors.put("price", "must be greater than 0");
        if (form.getCapacity() == null || form.getCapacity() <= 0)
            errors.put("capacity", "must be greater than 0");
        if (isBlank(form.getEventCategory()))
            errors.put("eventCategory", "fill mandatory fields");

        if (!errors.isEmpty())
            throw new ValidationException(errors);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
