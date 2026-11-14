package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.dto.EventUpdateResultDto;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.domain.exceptions.EventAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.EventCategoryNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventDateHasBookingsException;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import at.fhv.eventmangement.domain.exceptions.MaxParticipantsBelowBookedPlacesException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// REST endpoints for the organizer side
@RestController
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
    public ResponseEntity<EventUpdateResultDto> updateEvent(@PathVariable UUID id, @Valid @RequestBody EventForm form)
            throws EventNotFoundException, EventAlreadyExistsException, EventCategoryNotFoundException,
            EventDateHasBookingsException, MaxParticipantsBelowBookedPlacesException {
        return ResponseEntity.ok(eventService.updateEvent(id, form));
    }
}
