package at.fhv.eventmangement.infrastructure.mappers;

import at.fhv.eventmangement.domain.model.UserAccount;
import at.fhv.eventmangement.infrastructure.entities.UserAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {RoleEntityMapper.class, CustomerEntityMapper.class, EventOrganizerEntityMapper.class})
public interface UserAccountEntityMapper {
    UserAccount toDomain(UserAccountEntity entity);

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    UserAccountEntity toEntity(UserAccount domain);
}