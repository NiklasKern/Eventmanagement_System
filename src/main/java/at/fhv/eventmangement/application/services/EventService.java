package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.form.EventForm;
import jakarta.validation.Valid;

import java.util.List;

public interface EventService {
    List<EventDto> getEvents();
    EventDto addEvent(@Valid EventForm form);
}
