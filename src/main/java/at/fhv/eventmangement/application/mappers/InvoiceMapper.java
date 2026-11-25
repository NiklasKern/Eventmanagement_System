package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.InvoiceDto;
import at.fhv.eventmangement.application.dto.InvoiceItemDto;
import at.fhv.eventmangement.application.form.InvoiceForm;
import at.fhv.eventmangement.application.form.InvoiceItemForm;
import at.fhv.eventmangement.domain.model.Invoice;
import at.fhv.eventmangement.domain.model.InvoiceItem;
import org.mapstruct.Mapper;

import java.util.List;

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

    default List<InvoiceItem> toItems(List<InvoiceItemForm> forms) {
        if (forms == null) {
            return null;
        }
        return forms.stream()
                .map(f -> new InvoiceItem()
                        .setDescription(f.getDescription().trim())
                        .setQuantity(f.getQuantity())
                        .setUnitPrice(f.getUnitPrice())
                        .setVatRate(f.getVatRate()))
                .toList();
    }
}
