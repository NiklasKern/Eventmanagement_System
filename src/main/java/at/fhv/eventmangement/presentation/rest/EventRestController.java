package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.dto.EventUpdateResultDto;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.domain.exceptions.*;
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
    public EventDto getEventInfo(@PathVariable UUID id) throws EventNotFoundException
    {
        return eventService.getEventById(id);
    }

    // Updates an event and returns the saved event plus the customers that were notified
    @PutMapping(path = "/events/{id}")
    public ResponseEntity<EventUpdateResultDto> updateEvent(@PathVariable UUID id, @Valid @RequestBody EventForm form)
            throws EventNotFoundException, EventAlreadyExistsException, EventCategoryNotFoundException,
            EventDateHasBookingsException, MaxParticipantsBelowBookedPlacesException {
        return ResponseEntity.ok(eventService.updateEvent(id, form));
    }

}
