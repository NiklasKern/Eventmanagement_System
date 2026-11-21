package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.UserAccountDto;
import at.fhv.eventmangement.domain.model.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserAccountMapper {
    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleName", source = "role.name")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "organizerId", source = "organizer.id")
    UserAccountDto toDto(UserAccount account);
}
