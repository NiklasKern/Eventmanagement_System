package at.fhv.eventmangement.services;

import at.fhv.eventmangement.domain.dto.EventForm;
import at.fhv.eventmangement.domain.entities.Event;

import java.util.List;

public interface EventService {
    List<Event> getEvents();
    Event addEvent(EventForm form);
}
