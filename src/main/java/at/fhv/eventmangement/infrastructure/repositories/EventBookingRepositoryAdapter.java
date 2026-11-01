package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.EventBooking;
import at.fhv.eventmangement.domain.model.EventBookingStatus;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import at.fhv.eventmangement.infrastructure.entities.EventBookingEntity;
import at.fhv.eventmangement.infrastructure.mappers.EventBookingEntityMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class EventBookingRepositoryAdapter implements EventBookingRepository {

    private final EventBookingJpaRepository jpa;
    private final EventBookingEntityMapper mapper;

    public EventBookingRepositoryAdapter(EventBookingJpaRepository jpa, EventBookingEntityMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public EventBooking save(EventBooking eventBooking) {
        return mapper.toDomain(jpa.save(mapper.toEntity(eventBooking)));
    }

    @Override
    public boolean existsByBookingNumber(String bookingNumber) {
        return jpa.existsByBookingNumber(bookingNumber);
    }

    @Override
    public int sumBookedPlaces(UUID eventDateId) {
        return jpa.findByEventDateIdAndEventBookingStatusNot(eventDateId, EventBookingStatus.CANCELLED)
                .stream()
                .mapToInt(EventBookingEntity::getPlaces)
                .sum();
    }
}