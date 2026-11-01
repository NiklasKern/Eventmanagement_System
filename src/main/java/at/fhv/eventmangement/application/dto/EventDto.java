package at.fhv.eventmangement.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventDto(
        UUID id,
        String name,
        String description,
        String location,
        LocalDateTime dateTime,
        BigDecimal price,
        Integer minParticipants,
        Integer maxParticipants,
        String eventStatus,
        EventCategoryDto eventCategory,
        EventOrganizerDto eventOrganizer
) {
}
