package at.fhv.eventmangement.domain.exceptions;

// Thrown when a category with the same name already exists
public class EventCategoryAlreadyExistsException extends EventManagementException {
    public EventCategoryAlreadyExistsException() {
        super("category already exists");
    }
}
