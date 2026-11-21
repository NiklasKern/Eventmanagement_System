package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.RoleDto;
import at.fhv.eventmangement.domain.model.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleDto toDto(Role role);
}