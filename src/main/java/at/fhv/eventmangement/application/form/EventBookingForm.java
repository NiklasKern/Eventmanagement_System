package at.fhv.eventmangement.application.form;

import at.fhv.eventmangement.domain.model.EventBookingType;
import at.fhv.eventmangement.domain.model.PaymentMethod;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventBookingForm {

    @NotNull(message = "please fill mandatory fields")
    private UUID eventDateId;

    @NotNull(message = "please fill mandatory fields")
    private EventBookingType eventBookingType;

    // Only required for group bookings, see isPlacesValid
    @Positive(message = "must be greater than 0")
    private Integer places;

    @AssertTrue(message = "please enter the number of places")
    public boolean isPlacesValid() {
        return eventBookingType != EventBookingType.GROUP
                || (places != null && places > 0);
    }

    @NotNull(message = "please fill mandatory fields")
    private PaymentMethod paymentMethod;

}
