package at.fhv.eventmangement.domain.exceptions;

public class RoleInUseException extends EventManagementException {
    public RoleInUseException() {
        super("This role is already in use");
    }
}
