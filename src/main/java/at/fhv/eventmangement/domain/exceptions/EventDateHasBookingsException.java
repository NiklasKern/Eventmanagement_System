package at.fhv.eventmangement.domain.exceptions;

// Thrown when a date that still has active bookings should be removed from an event
public class EventDateHasBookingsException extends EventManagementException {
    public EventDateHasBookingsException() {
        super("a date with bookings cannot be removed");
    }
}
