package at.fhv.eventmangement.application.exceptions;

// Thrown when the selected category does not exist
public class EventCategoryNotFoundException extends EventManagementException {
    public EventCategoryNotFoundException() {
        super("unknown category");
    }
}
