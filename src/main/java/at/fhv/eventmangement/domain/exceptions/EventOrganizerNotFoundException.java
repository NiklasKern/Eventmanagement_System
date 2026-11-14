package at.fhv.eventmangement.domain.exceptions;

// Thrown when the (default) organizer does not exist
public class EventOrganizerNotFoundException extends EventManagementException {
    public EventOrganizerNotFoundException() {
        super("organizer does not exist");
    }
}
