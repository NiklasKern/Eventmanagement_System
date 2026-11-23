package at.fhv.eventmangement.domain.repositories;

import at.fhv.eventmangement.domain.model.Invoice;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(UUID id);
    Optional<Invoice> findByBookingId(UUID bookingId);
}
