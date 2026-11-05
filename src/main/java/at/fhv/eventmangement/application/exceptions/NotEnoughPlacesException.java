package at.fhv.eventmangement.application.exceptions;

// Thrown when the booking needs more places than are left
public class NotEnoughPlacesException extends EventManagementException {
    private final int placesLeft;

    public NotEnoughPlacesException(int placesLeft) {
        super("only " + placesLeft + " places left");
        this.placesLeft = placesLeft;
    }

    public int getPlacesLeft() {
        return placesLeft;
    }
}
