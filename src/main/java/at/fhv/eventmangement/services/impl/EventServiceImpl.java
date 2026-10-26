package at.fhv.eventmangement.services.impl;

import at.fhv.eventmangement.domain.dto.EventForm;
import at.fhv.eventmangement.domain.entities.Event;
import at.fhv.eventmangement.domain.entities.EventStatus;
import at.fhv.eventmangement.repositories.EventRepository;
import at.fhv.eventmangement.services.EventService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
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

        //Throw error if name is not set
        if(form.getName() == null || form.getName().isBlank())
        {
            throw new IllegalArgumentException("Event name cannot be empty or null");
        }

        //Throw error if dateTime is not set
        if(form.getDateTime()==null)
        {
            throw new IllegalArgumentException("Event date cannot be empty or null");
        }

        //Throw error if price is not set
        if(form.getPrice() == null || form.getPrice() <= 0)
        {
            throw new IllegalArgumentException("Price cannot be empty or <= 0");
        }

        //Throw error if capacity is not set
        if(form.getCapacity() == null || form.getCapacity() <= 0)
        {
            throw new IllegalArgumentException("Capacity cannot be empty or <= 0");
        }

        //Throw error if event category is not set
        if(form.getEventCategory() == null || form.getEventCategory().isEmpty())
        {
            throw new IllegalArgumentException("Event category cannot be empty or null");
        }

        LocalDateTime now = LocalDateTime.now();

        //Create new event to prevent unexpected input
        return eventRepository.save(new Event(
                null,
                form.getName(),
                Optional.ofNullable(form.getDescription()).orElse(""),
                form.getDateTime(),
                form.getPrice(),
                form.getCapacity(),
                EventStatus.OPEN,
                form.getEventCategory(),
                now,
                now
        ));
    }
}
