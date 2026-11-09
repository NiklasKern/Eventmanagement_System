package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.EventBookingStatus;
import at.fhv.eventmangement.infrastructure.entities.EventBookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventBookingJpaRepository extends JpaRepository<EventBookingEntity, UUID> {
    // Cancelled bookings free their places again
    List<EventBookingEntity> findByEventDateIdAndEventBookingStatusNot(UUID eventDateId, EventBookingStatus status);
    boolean existsByBookingNumber(String bookingNumber);
    boolean existsByEventDateIdAndEventBookingStatusNotIn(UUID eventDateId, Collection<EventBookingStatus> statuses);
}
