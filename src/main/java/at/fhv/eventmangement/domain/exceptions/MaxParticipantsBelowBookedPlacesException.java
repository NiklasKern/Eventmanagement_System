package at.fhv.eventmangement.domain.exceptions;

// Thrown when maxParticipants should be set below the places that are already booked for a date
public class MaxParticipantsBelowBookedPlacesException extends EventManagementException {
    private final int bookedPlaces;

    public MaxParticipantsBelowBookedPlacesException(int bookedPlaces) {
        super("max participants cannot be lower than the already booked places (" + bookedPlaces + ")");
        this.bookedPlaces = bookedPlaces;
    }

    public int getBookedPlaces() {
        return bookedPlaces;
    }
}
