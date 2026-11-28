package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.InvoiceDto;
import at.fhv.eventmangement.application.dto.InvoiceItemDto;
import at.fhv.eventmangement.application.form.InvoiceForm;
import at.fhv.eventmangement.domain.model.Invoice;
import at.fhv.eventmangement.domain.model.InvoiceItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = ContactInfoMapper.class)
public interface InvoiceMapper {
    InvoiceDto toDto(Invoice invoice);
    InvoiceItemDto toDto(InvoiceItem item);

    default InvoiceForm toForm(InvoiceDto invoice) {
        return InvoiceForm.builder()
                .name(invoice.contactInfo() == null ? null : invoice.contactInfo().name())
                .address(invoice.contactInfo() == null ? null : invoice.contactInfo().address())
                .email(invoice.contactInfo() == null ? null : invoice.contactInfo().email())
                .paymentMethod(invoice.paymentMethod())
                .build();
    }
}