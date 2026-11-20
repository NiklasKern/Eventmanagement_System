package at.fhv.eventmangement.domain.exceptions;

public class UserAccountAlreadyExistsException extends EventManagementException {
    public UserAccountAlreadyExistsException() {
        super("Username already exists");
    }
}
