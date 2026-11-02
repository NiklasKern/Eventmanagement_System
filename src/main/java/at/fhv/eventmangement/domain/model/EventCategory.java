package at.fhv.eventmangement.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class EventCategory {
    private UUID id;
    private String categoryName;
    private LocalDateTime created;
    private LocalDateTime updated;
}
