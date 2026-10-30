package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.EventBooking;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventBookingJpaRepository extends JpaRepository<EventBooking, UUID>, EventBookingRepository {

    // Cancelled bookings free their places again
    @Override
    @Query("""
            select coalesce(sum(b.places), 0) from EventBooking b
            where b.eventDate.id = :eventDateId
            and b.eventBookingStatus <> at.fhv.eventmangement.domain.model.EventBookingStatus.CANCELLED
            """)
    int sumBookedPlaces(@Param("eventDateId") UUID eventDateId);
}
