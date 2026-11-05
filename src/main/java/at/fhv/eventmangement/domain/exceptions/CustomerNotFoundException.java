package at.fhv.eventmangement.domain.exceptions;

// Thrown when the (default) customer does not exist
public class CustomerNotFoundException extends EventManagementException {
    public CustomerNotFoundException() {
        super("customer does not exist");
    }
}
