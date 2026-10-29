package at.fhv.eventmangement.exceptions;

//Base for all application specific exceptions
//Since it extends RuntimeExceptions it doesnt need to throw declarations.
public class EventManagementException extends RuntimeException {
    public EventManagementException(String message) {
        super(message);
    }
}
