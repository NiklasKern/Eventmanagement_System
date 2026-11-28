package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.application.dto.EventOrganizerDto;
import at.fhv.eventmangement.application.form.EventOrganizerForm;
import at.fhv.eventmangement.domain.exceptions.EventOrganizerAlreadyExistsException;

import java.util.List;

public interface EventOrganizerService {
    void addOrganizer(EventOrganizerForm form) throws EventOrganizerAlreadyExistsException;

    // For the organizer dropdowns (create event, user manager)
    List<EventOrganizerDto> getAllOrganizers();
}