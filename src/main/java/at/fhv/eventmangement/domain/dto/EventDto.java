package at.fhv.eventmangement.domain.dto;

import at.fhv.eventmangement.domain.entities.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventDto(
        UUID id,
        String name,
        String description,
        String organizer,
        String location,
        LocalDateTime dateTime,
        BigDecimal price,
        Integer minParticipants,
        Integer maxParticipants,
        EventStatus eventStatus,
        EventCategoryDto eventCategory,
        EventOrganizerDto eventOrganizer
) {
}
