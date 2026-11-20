package at.fhv.eventmangement.domain.exceptions;

public class RoleInUseException extends EventManagementException {
    public RoleInUseException(String message) {
        super("This role is already in use");
    }
}
