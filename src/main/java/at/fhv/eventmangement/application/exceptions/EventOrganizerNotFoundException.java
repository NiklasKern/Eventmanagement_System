package at.fhv.eventmangement.application.exceptions;

// Thrown when the (default) organizer does not exist
public class EventOrganizerNotFoundException extends EventManagementException {
    public EventOrganizerNotFoundException() {
        super("organizer does not exist");
    }
}
