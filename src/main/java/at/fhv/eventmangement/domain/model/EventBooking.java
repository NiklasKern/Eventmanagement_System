package at.fhv.eventmangement.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder
@AllArgsConstructor
public class EventBooking extends BaseModel {
    private String bookingNumber;
    private Customer customer;
    private EventDate eventDate;
    private EventBookingType eventBookingType;
    private Integer places;
    private BigDecimal price;
    private PaymentMethod paymentMethod;
    private LocalDate optionDate;
    private LocalDate cancellationDate;
    private EventBookingStatus eventBookingStatus;
    @Builder.Default
    private List<EventParticipant> eventParticipants = new ArrayList<>();
}