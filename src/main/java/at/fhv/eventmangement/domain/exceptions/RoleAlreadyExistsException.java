package at.fhv.eventmangement.domain.exceptions;

public class RoleAlreadyExistsException extends EventManagementException {
    public RoleAlreadyExistsException(String message) {
        super("This role already exists");
    }
}
