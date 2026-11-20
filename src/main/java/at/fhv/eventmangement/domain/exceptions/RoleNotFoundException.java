package at.fhv.eventmangement.domain.exceptions;

public class RoleNotFoundException extends EventManagementException {
    public RoleNotFoundException() {
        super("This role does not exist");
    }
}
