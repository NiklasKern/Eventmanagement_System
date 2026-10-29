package at.fhv.eventmangement.domain.dto;

import java.util.UUID;

public record EventCategoryDto(
        UUID id,
        String categoryName
) {
}
