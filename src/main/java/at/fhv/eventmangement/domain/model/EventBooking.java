package at.fhv.eventmangement.domain.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Table(name = "event_booking")
public class EventBooking extends BaseEntity{

    @Column(name = "booking_number", nullable = false, unique = true, updatable = false, length = 20)
    private String bookingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_date_id")
    private EventDate eventDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_booking_type", nullable = false)
    private EventBookingType eventBookingType;

    @Column(name = "places", nullable = false)
    private Integer places;

    @Column(name = "price",nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method",nullable = false)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.CARD;

    @Column(name = "option_date")
    private LocalDate optionDate;

    @Column(name = "cancellation_date")
    private LocalDate cancellationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_booking_status",nullable = false)
    @Builder.Default
    private EventBookingStatus eventBookingStatus = EventBookingStatus.OPTION;

    @OneToMany(mappedBy = "eventBooking", fetch = FetchType.LAZY)
    @Builder.Default
    private List<EventParticipant> eventParticipants = new ArrayList<>();
}
