package at.fhv.eventmangement.application.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventForm {

    @NotBlank(message = "please fill mandatory fields")
    private String name;

    private String description;

    @NotBlank(message = "please fill mandatory fields")
    private String location;

    @NotNull(message = "please fill mandatory fields")
    @Positive(message = "must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "please fill mandatory fields")
    @Positive(message = "must be greater than 0")
    private Integer minParticipants;

    @NotNull(message = "please fill mandatory fields")
    @Positive(message = "must be greater than 0")
    private Integer maxParticipants;

    @AssertTrue(message = "must not be less than minimum participants")
    public boolean isMaxParticipantsValid() {
        return minParticipants == null || maxParticipants == null
                || maxParticipants >= minParticipants;
    }

    @NotNull(message = "please fill mandatory fields")
    private UUID eventCategoryId;

    @NotEmpty(message = "please add at least one date")
    @Builder.Default
    private List<@Valid EventDateForm> eventDates = new ArrayList<>();

}
