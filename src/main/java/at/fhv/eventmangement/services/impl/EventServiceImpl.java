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
    public Event addEvent(Event event) {


//        Implement validation logic

//        if(event.getName() == null || event.getName().isBlank())
//        {
//            throw new IllegalArgumentException("Event name cannot be empty or null");
//        }


        LocalDateTime now = LocalDateTime.now();

        return eventRepository.save(new Event(
                null,
                event.getName(),
                event.getDescription(),
                event.getDateTime(),
                event.getPrice(),
                event.getCapacity(),
                event.getEventStatus(),
                event.getEventCategory(),
                now,
                now
        ));
    }
}
