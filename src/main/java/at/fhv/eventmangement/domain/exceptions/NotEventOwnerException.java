package at.fhv.eventmangement.domain.exceptions;

public class NotEventOwnerException extends EventManagementException {
    public NotEventOwnerException() {
        super("You can only edit your own events");
    }
}