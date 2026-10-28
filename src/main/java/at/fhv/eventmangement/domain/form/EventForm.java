package at.fhv.eventmangement.domain.form;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @NotNull(message = "please fill mandatory fields")
    @Future(message = "Date has to be in the future")
    private LocalDateTime dateTime;

    @NotNull
    private UUID eventCategoryId;

}
