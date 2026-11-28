package at.fhv.eventmangement.domain.exceptions;

public class CustomerNotFoundException extends EventManagementException {
    public CustomerNotFoundException() {
        super("customer does not exist");
    }
    
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
