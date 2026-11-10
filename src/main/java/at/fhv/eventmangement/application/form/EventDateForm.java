package at.fhv.eventmangement.application.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDateForm {

    @NotNull(message = "please fill mandatory fields")
    @Future(message = "Date has to be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @NotNull(message = "please fill mandatory fields")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    @AssertTrue(message = "event end has to be after event start")
    public boolean isEndTimeValid() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }
}
