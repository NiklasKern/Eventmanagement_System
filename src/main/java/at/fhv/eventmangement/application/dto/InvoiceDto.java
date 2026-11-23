package at.fhv.eventmangement.application.dto;

import at.fhv.eventmangement.domain.model.InvoiceStatus;
import at.fhv.eventmangement.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceDto(
        UUID id,
        UUID bookingId,
        LocalDateTime invoiceDate,
        ContactInfoDto contactInfo,
        InvoiceStatus invoiceStatus,
        PaymentMethod paymentMethod,
        List<InvoiceItemDto> items,
        BigDecimal netTotal,
        BigDecimal vatTotal,
        BigDecimal grossTotal,
        BigDecimal advancePayment,
        BigDecimal remainingAmount,
        boolean editable
) {
}
