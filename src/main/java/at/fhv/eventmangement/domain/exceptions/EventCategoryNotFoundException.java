package at.fhv.eventmangement.domain.exceptions;

// Thrown when the selected category does not exist
public class EventCategoryNotFoundException extends EventManagementException {
    public EventCategoryNotFoundException() {
        super("unknown category");
    }
}
