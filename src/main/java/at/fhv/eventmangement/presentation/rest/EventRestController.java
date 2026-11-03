package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.application.services.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class EventRestController {

    private final EventService eventService;

    public EventRestController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping(path = "/getEvents")
    public List<EventDto> getEvents()
    {
        return eventService.getAllEvents();
    }

    @GetMapping("/customer/event/{id}/info")
    public EventDto getEventInfo(@PathVariable UUID id)
    {
        return eventService.getEventById(id);
    }

    @GetMapping(path = "/events/{id}")
    public ResponseEntity<EventDto> getEventById(@PathVariable UUID id) {
        EventDto event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }
    @PutMapping(path = "/events/{id}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable UUID id, @Valid @RequestBody EventForm form) {
        EventDto updatedEvent = eventService.updateEvent(id, form);
        return ResponseEntity.ok(updatedEvent);
    }

}
