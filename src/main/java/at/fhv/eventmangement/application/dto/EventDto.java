package at.fhv.eventmangement.application.dto;

import at.fhv.eventmangement.domain.model.EventStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record EventDto(
        UUID id,
        String name,
        String description,
        String location,
        BigDecimal price,
        Integer minParticipants,
        Integer maxParticipants,
        EventStatus eventStatus,
        EventCategoryDto eventCategory,
        EventOrganizerDto eventOrganizer,
        List<EventDateDto> eventDates
) {
}
