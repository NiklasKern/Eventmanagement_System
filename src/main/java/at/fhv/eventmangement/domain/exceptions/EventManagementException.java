package at.fhv.eventmangement.domain.exceptions;

//Base for all application specific exceptions
//Since it extends RuntimeExceptions it doesnt need to throw declarations.
public class EventManagementException extends RuntimeException {
    protected EventManagementException(String message) {
        super(message);
    }
}
