package at.fhv.eventmangement.domain.repositories;

import at.fhv.eventmangement.domain.model.EventBooking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Optional;

public interface EventBookingRepository {
    boolean existsByBookingNumber(String bookingNumber);
    EventBooking save(EventBooking eventBooking);
    Optional<EventBooking> findById(UUID id);
    int sumBookedPlaces(UUID eventDateId);
    boolean hasActiveBookings(UUID eventDateId);
    List<EventBooking> findActiveBookingsByEventId(UUID eventId);
    List<EventBooking> findByCustomerId(UUID customerId);
    Optional<EventBooking> findByBookingNumber(String bookingNumber);
}
