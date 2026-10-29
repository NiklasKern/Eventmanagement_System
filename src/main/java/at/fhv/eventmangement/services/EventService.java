package at.fhv.eventmangement.services;

import at.fhv.eventmangement.domain.entities.Event;
import at.fhv.eventmangement.domain.form.EventForm;
import jakarta.validation.Valid;

import java.util.List;

public interface EventService {
    List<Event> getEvents();
    Event addEvent(@Valid EventForm form);
}
