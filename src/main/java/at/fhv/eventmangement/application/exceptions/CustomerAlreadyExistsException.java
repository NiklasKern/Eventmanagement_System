package at.fhv.eventmangement.application.exceptions;

// Thrown when a customer with the same name already exists
public class CustomerAlreadyExistsException extends EventManagementException {
    public CustomerAlreadyExistsException() {
        super("customer already exists");
    }
}
