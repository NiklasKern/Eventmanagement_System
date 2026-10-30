package at.fhv.eventmangement.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Builder
@AllArgsConstructor
public class EventOrganizer {
    private UUID id;
    private ContactInfo contactInfo;
    private LocalDateTime created;
    private LocalDateTime updated;
}
