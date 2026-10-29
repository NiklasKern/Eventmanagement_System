package at.fhv.eventmangement.controllers.rest;

import at.fhv.eventmangement.domain.dto.EventDto;
import at.fhv.eventmangement.domain.entities.Event;
import at.fhv.eventmangement.mappers.EventMapper;
import at.fhv.eventmangement.services.EventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

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

    @GetMapping(path = "/events/{id}")
    public ResponseEntity<EventDto> getEventById(@PathVariable UUID id) {
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(eventMapper.toDto(event));
    }
}
