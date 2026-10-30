package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.ContactInfoDto;
import at.fhv.eventmangement.domain.model.ContactInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContactInfoMapper {
    ContactInfo fromDto(ContactInfoDto contactInfoDto);
    ContactInfoDto toDto(ContactInfo contactInfo);
}
