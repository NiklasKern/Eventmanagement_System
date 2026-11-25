package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.infrastructure.entities.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, UUID> {
    Optional<InvoiceEntity> findByBookingId(UUID bookingId);
}
