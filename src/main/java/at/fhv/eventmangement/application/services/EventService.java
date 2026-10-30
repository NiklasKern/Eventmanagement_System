package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.form.EventForm;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface EventService {
    List<EventDto> getAllEvents();
    List<EventDto> getActiveEvents();
    EventDto addEvent(@Valid EventForm form);
    EventDto getEventById(UUID id);
}
