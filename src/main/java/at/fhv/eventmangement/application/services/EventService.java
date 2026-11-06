package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.domain.exceptions.EventAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.EventCategoryNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventOrganizerNotFoundException;
import at.fhv.eventmangement.application.form.EventForm;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface EventService {
    List<EventDto> getAllEvents();
    List<EventDto> getActiveEvents();
    EventDto addEvent(@Valid EventForm form)
            throws EventAlreadyExistsException, EventCategoryNotFoundException, EventOrganizerNotFoundException;
    EventDto getEventById(UUID id) throws EventNotFoundException;
}
