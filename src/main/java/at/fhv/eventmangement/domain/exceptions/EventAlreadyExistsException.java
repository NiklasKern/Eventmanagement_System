package at.fhv.eventmangement.domain.exceptions;

// Thrown when an event with the same name already exists
public class EventAlreadyExistsException extends EventManagementException {
    public EventAlreadyExistsException() {
        super("event name already exists");
    }
}
