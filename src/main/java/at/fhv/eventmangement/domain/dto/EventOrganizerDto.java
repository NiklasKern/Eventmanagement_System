package at.fhv.eventmangement.domain.dto;

import java.util.UUID;

public record EventOrganizerDto(
        UUID id,
        String organizerName,
        String address,
        String email
) {
}
