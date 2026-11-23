package at.fhv.eventmangement.application.dto;

public record InvoiceReviewDto(
        InvoiceDto invoice,
        EventBookingDto booking,
        EventDto event
) {
}
