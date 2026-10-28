package at.fhv.eventmangement.domain.dto;

import at.fhv.eventmangement.domain.entities.EventStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventDto(
        UUID id,
        String name,
        String description,
        String organizer,
        String location,
        LocalDateTime dateTime,
        Float price,
        Integer capacity,
        EventStatus eventStatus,
        String eventCategory
) {
}
