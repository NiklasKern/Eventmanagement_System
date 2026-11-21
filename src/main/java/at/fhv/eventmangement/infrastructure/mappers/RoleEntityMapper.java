package at.fhv.eventmangement.infrastructure.mappers;

import at.fhv.eventmangement.domain.model.Role;
import at.fhv.eventmangement.infrastructure.entities.RoleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleEntityMapper {
    Role toDomain(RoleEntity entity);
    RoleEntity toEntity(Role domain);
}
