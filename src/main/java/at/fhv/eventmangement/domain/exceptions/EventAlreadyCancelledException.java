package at.fhv.eventmangement.domain.exceptions;

public class EventAlreadyCancelledException extends Exception {
    public EventAlreadyCancelledException() {
        super("Event is already cancelled.");
    }
}