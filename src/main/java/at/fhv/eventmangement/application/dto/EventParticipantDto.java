package at.fhv.eventmangement.application.dto;

import java.util.UUID;

public record EventParticipantDto(
        UUID id,
        CustomerDto customer,
        String name,
        boolean checkedIn,
        boolean checkedOut
) {
}
