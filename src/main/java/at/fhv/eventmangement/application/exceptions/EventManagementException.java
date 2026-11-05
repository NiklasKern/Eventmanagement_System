package at.fhv.eventmangement.application.exceptions;

// Base for all application specific exceptions.
// Checked on purpose: every caller has to declare or handle each exception explicitly.
public abstract class EventManagementException extends Exception {
    protected EventManagementException(String message) {
        super(message);
    }
}
