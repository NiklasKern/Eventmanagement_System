package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.domain.exceptions.EventOrganizerAlreadyExistsException;
import at.fhv.eventmangement.application.form.EventOrganizerForm;

public interface EventOrganizerService {
    void addOrganizer(EventOrganizerForm form) throws EventOrganizerAlreadyExistsException;
}
