package at.fhv.eventmangement.services;

import at.fhv.eventmangement.domain.dto.EventForm;
import at.fhv.eventmangement.domain.entities.Event;
import jakarta.validation.Valid;
import java.util.UUID;

import java.util.List;

public interface EventService {
    List<Event> getEvents();
    Event addEvent(@Valid EventForm form);
    List<String> getEventCategories();
    Event getEventById(UUID id);
}
