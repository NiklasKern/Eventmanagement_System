package at.fhv.eventmangement.domain.repositories;

import at.fhv.eventmangement.domain.model.EventStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record EventSearchCriteria(
        String text,
        String location,
        UUID categoryId,
        LocalDateTime from,
        LocalDateTime to,
        BigDecimal priceFrom,
        BigDecimal priceTo,
        EventStatus status) {
}
