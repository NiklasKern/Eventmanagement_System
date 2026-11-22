package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.dto.EventUpdateResultDto;
import at.fhv.eventmangement.application.form.EventSearchForm;
import at.fhv.eventmangement.domain.exceptions.*;
import at.fhv.eventmangement.application.form.EventForm;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface EventService {
    List<EventDto> getAllEvents();

    List<EventDto> getActiveEvents();

    List<EventDto> searchEvents(@Valid EventSearchForm searchForm);

    List<String> getEventLocation();

    EventDto getEventById(UUID id) throws EventNotFoundException;

    List<EventDto> getManagedEvents(String username);

    EventDto addEvent(@Valid EventForm form, String username)
            throws EventAlreadyExistsException, EventCategoryNotFoundException, EventOrganizerNotFoundException;

    EventDto getEventForEdit(UUID id, String username) throws EventNotFoundException, NotEventOwnerException;

    EventUpdateResultDto updateEvent(UUID id, @Valid EventForm form, String username)
            throws EventNotFoundException, EventAlreadyExistsException, EventCategoryNotFoundException,
            EventDateHasBookingsException, MaxParticipantsBelowBookedPlacesException, NotEventOwnerException;

    EventUpdateResultDto cancelEvent(UUID id)
            throws EventNotFoundException, at.fhv.eventmangement.domain.exceptions.EventAlreadyCancelledException;
}
