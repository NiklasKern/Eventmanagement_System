package at.fhv.eventmangement.application.exceptions;

// Thrown when no event exists for the given id
public class EventNotFoundException extends EventManagementException {
    public EventNotFoundException() {
        super("event does not exist");
    }
}
