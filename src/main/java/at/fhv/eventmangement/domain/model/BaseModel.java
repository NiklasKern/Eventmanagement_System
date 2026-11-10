package at.fhv.eventmangement.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class BaseModel {
    private UUID id;
    private LocalDateTime created;
    private LocalDateTime updated;
}