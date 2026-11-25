package at.fhv.eventmangement.application.dto;

import java.math.BigDecimal;

public record InvoiceItemDto(
        String description,
        Integer quantity,
        BigDecimal unitPrice,
        Integer vatRate,
        BigDecimal netAmount,
        BigDecimal vatAmount,
        BigDecimal grossAmount
) {
}
