package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.Invoice;
import at.fhv.eventmangement.domain.repositories.InvoiceRepository;
import at.fhv.eventmangement.infrastructure.mappers.InvoiceEntityMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class InvoiceRepositoryAdapter implements InvoiceRepository {

    private final InvoiceJpaRepository jpa;
    private final InvoiceEntityMapper mapper;

    public InvoiceRepositoryAdapter(InvoiceJpaRepository jpa, InvoiceEntityMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Invoice save(Invoice invoice) {
        return mapper.toDomain(jpa.save(mapper.toEntity(invoice)));
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Invoice> findByBookingId(UUID bookingId) {
        return jpa.findByBookingId(bookingId).map(mapper::toDomain);
    }
}
