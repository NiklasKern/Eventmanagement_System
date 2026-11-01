package at.fhv.eventmangement.infrastructure.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Table(name = "event_participant")
public class EventParticipantEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_booking_id")
    private EventBookingEntity eventBooking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "checked_in")
    private boolean checkedIn;

    @Column(name = "checked_out")
    private boolean checkedOut;
}
