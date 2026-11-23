package at.fhv.eventmangement.infrastructure.mappers;

import at.fhv.eventmangement.domain.model.Invoice;
import at.fhv.eventmangement.domain.model.InvoiceItem;
import at.fhv.eventmangement.infrastructure.entities.InvoiceEntity;
import at.fhv.eventmangement.infrastructure.entities.InvoiceItemEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InvoiceEntityMapper {
    Invoice toDomain(InvoiceEntity entity);
    InvoiceEntity toEntity(Invoice domain);

    @Mapping(target = "invoiceId", source = "invoice.id")
    InvoiceItem toDomain(InvoiceItemEntity entity);

    @Mapping(target = "invoice", ignore = true)
    InvoiceItemEntity toEntity(InvoiceItem domain);

    @AfterMapping
    default void linkItems(@MappingTarget InvoiceEntity invoice) {
        if (invoice.getItems() != null) {
            invoice.getItems().forEach(item -> item.setInvoice(invoice));
        }
    }
}
