package at.fhv.eventmangement.domain.repositories;

import at.fhv.eventmangement.domain.model.EventBooking;

import java.util.List;
import java.util.UUID;

public interface EventBookingRepository {
    boolean existsByBookingNumber(String bookingNumber);
    EventBooking save(EventBooking eventBooking);
    int sumBookedPlaces(UUID eventDateId);
    boolean hasActiveBookings(UUID eventDateId);
    List<EventBooking> findActiveBookingsByEventId(UUID eventId);
}
