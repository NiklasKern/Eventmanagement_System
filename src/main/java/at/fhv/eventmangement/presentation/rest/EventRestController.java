package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.dto.EventUpdateResultDto;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.domain.exceptions.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

// REST endpoints for the organizer side
@RestController
@RequestMapping("/api")
public class EventRestController {

    private final EventService eventService;

    public EventRestController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/events")
    public List<EventDto> getEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/events/{id}")
    public EventDto getEventInfo(@PathVariable UUID id) throws EventNotFoundException {
        return eventService.getEventById(id);
    }

    // Updates an event and returns the saved event plus the customers that were notified
    @PutMapping("/events/{id}")
    // Providers may only change their own events (NotEventOwnerException → 403)
    public ResponseEntity<EventUpdateResultDto> updateEvent(@PathVariable UUID id, @Valid @RequestBody EventForm form,
                                                            Authentication authentication)
            throws EventNotFoundException, EventAlreadyExistsException, EventCategoryNotFoundException,
            EventDateHasBookingsException, MaxParticipantsBelowBookedPlacesException, NotEventOwnerException {
        return ResponseEntity.ok(eventService.updateEvent(id, form, authentication.getName()));
    }
}