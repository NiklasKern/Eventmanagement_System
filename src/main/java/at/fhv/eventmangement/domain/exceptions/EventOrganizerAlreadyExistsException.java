package at.fhv.eventmangement.domain.exceptions;

// Thrown when an organizer with the same name already exists
public class EventOrganizerAlreadyExistsException extends EventManagementException {
    public EventOrganizerAlreadyExistsException() {
        super("organizer name already exists");
    }
}
