package at.fhv.eventmangement.domain.exceptions;

public class RoleAlreadyExistsException extends EventManagementException {
    public RoleAlreadyExistsException() {
        super("This role already exists");
    }
}