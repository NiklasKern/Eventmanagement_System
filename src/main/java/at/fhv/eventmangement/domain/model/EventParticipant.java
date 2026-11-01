package at.fhv.eventmangement.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
public class EventParticipant extends BaseModel {
    private EventBooking eventBooking;
    private Customer customer;
    private String name;
    private boolean checkedIn;
    private boolean checkedOut;
}