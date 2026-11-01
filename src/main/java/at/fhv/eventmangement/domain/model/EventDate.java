package at.fhv.eventmangement.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@AllArgsConstructor
public class EventDate extends BaseModel {
    private Event event;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}