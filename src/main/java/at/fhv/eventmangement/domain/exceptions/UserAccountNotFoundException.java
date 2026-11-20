package at.fhv.eventmangement.domain.exceptions;

public class UserAccountNotFoundException extends EventManagementException {
    public UserAccountNotFoundException(String message) {
        super(message);
    }
}
