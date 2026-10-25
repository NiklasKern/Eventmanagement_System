package at.fhv.eventmangement.controllers;

import at.fhv.eventmangement.domain.dto.EventDto;
import at.fhv.eventmangement.domain.entities.Event;
import at.fhv.eventmangement.mappers.EventMapper;
import at.fhv.eventmangement.services.EventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
//@RequestMapping(path = "/event")
public class EventController {

    private final EventService eventService;
    private final EventMapper eventMapper;

    public EventController(EventService eventService, EventMapper eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    @PostMapping(path = "/add-event")
    public EventDto addEvent(@RequestBody EventDto eventDto)
    {
        Event newEvent = eventMapper.fromDto(eventDto);
        eventService.addEvent(newEvent);
        return eventMapper.toDto(newEvent);
    }

    @GetMapping(path = "/events")
    public List<EventDto> getEvents()
    {
        return eventService.getEvents().stream().map(eventMapper::toDto).toList();
    }

}
