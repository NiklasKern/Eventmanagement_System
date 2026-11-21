package at.fhv.eventmangement.application.dto;

import java.util.UUID;

public record UserAccountDto(UUID id,
                            String username,
                            boolean enabled,
                            UUID roleId,
                            String roleName,
                            UUID customerId,
                            UUID organizerId) {
}
