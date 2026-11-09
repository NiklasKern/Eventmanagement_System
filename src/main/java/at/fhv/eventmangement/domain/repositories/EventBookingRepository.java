package at.fhv.eventmangement.domain.repositories;

import at.fhv.eventmangement.domain.model.EventBooking;
import at.fhv.eventmangement.domain.model.EventBookingStatus;

import java.util.List;
import java.util.UUID;

public interface EventBookingRepository {
    boolean existsByBookingNumber(String bookingNumber);
    EventBooking save(EventBooking eventBooking);
    int sumBookedPlaces(UUID eventDateId);
    boolean hasActiveBookings(UUID eventDateId);

    @Override
    public List<EventBooking> findActiveBookingsByEventId(UUID eventId) {
        return jpa.findActiveByEventId(eventId, List.of(EventBookingStatus.CANCELLED, EventBookingStatus.EXPIRED))
                .stream().map(mapper::toDomain).toList();
    }
}
