package at.fhv.eventmangement.application.dto;

import at.fhv.eventmangement.domain.model.EventBookingStatus;
import at.fhv.eventmangement.domain.model.EventBookingType;
import at.fhv.eventmangement.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EventBookingDto(
        UUID id,
        String bookingNumber,
        CustomerDto customer,
        EventDateDto eventDate,
        EventBookingType eventBookingType,
        Integer places,
        BigDecimal price,
        PaymentMethod paymentMethod,
        LocalDate optionDate,
        LocalDate cancellationDate,
        EventBookingStatus eventBookingStatus,
        List<EventParticipantDto> eventParticipants
) {
}
