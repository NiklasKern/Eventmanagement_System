package at.fhv.eventmangement.application.dto;

import java.util.UUID;

public record EventOrganizerDto(
        UUID id,
        ContactInfoDto contactInfo
) {
}
