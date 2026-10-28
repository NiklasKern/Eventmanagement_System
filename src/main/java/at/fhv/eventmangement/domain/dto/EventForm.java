package at.fhv.eventmangement.domain.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

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
    @Future(message = "Date has to be in the future")
    private LocalDateTime dateTime;

    @Positive(message = "must be greater than 0")
    private Float price;

    @Positive(message = "must be greater than 0")
    private Integer capacity;

    @NotNull
    private UUID eventCategoryId;

    @NotBlank(message = "please fill mandatory fields")
    private String organizer;

}
