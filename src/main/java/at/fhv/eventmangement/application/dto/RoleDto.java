package at.fhv.eventmangement.application.dto;

import at.fhv.eventmangement.domain.model.Permissions;

import java.util.Set;
import java.util.UUID;

public record RoleDto (UUID id,
                       String name,
                       String description,
                       Set<Permissions> permissions,
                       boolean builtIn) {
}
