package at.fhv.eventmangement.controllers.rest;

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
public class EventRestController {

    private final EventService eventService;
    private final EventMapper eventMapper;

    public EventRestController(EventService eventService, EventMapper eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    @GetMapping(path = "/getEvents")
    public List<EventDto> getEvents()
    {
        return eventService.getEvents().stream().map(eventMapper::toDto).toList();
    }

}
