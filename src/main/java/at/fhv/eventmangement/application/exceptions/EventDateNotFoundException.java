package at.fhv.eventmangement.application.exceptions;

// Thrown when the selected date does not exist or does not belong to the event
public class EventDateNotFoundException extends EventManagementException {
    public EventDateNotFoundException() {
        super("unknown event date");
    }
}
