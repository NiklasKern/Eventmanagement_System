package at.fhv.eventmangement.domain.exceptions;

public class RoleNotFoundException extends EventManagementException {
    public RoleNotFoundException(String message) {
        super("This role does not exists");
    }
}
