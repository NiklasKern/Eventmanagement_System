package at.fhv.eventmangement.services;

import at.fhv.eventmangement.domain.form.EventOrganizerForm;
import at.fhv.eventmangement.domain.entities.EventOrganizer;

public interface EventOrganizerService {
    public EventOrganizer addOrganizer(EventOrganizerForm form);
}
