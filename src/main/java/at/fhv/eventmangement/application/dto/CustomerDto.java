package at.fhv.eventmangement.application.dto;

import at.fhv.eventmangement.domain.model.CustomerType;

import java.util.UUID;

public record CustomerDto(
        UUID id,
        ContactInfoDto contactInfo,
        String phone,
        CustomerType customerType
) {
}
