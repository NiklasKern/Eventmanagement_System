package at.fhv.eventmangement.application.exceptions;

// Thrown when the event is not open for booking
public class EventNotBookableException extends EventManagementException {
    public EventNotBookableException() {
        super("event is not open for booking");
    }
}
