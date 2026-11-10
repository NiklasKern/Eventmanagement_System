package at.fhv.eventmangement.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventDateDto(
        UUID id,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
