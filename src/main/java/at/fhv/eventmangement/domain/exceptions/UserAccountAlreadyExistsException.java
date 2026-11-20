package at.fhv.eventmangement.domain.exceptions;

public class UserAccountAlreadyExistsException extends RuntimeException {
    public UserAccountAlreadyExistsException(String message) {
        super(message);
    }
}
