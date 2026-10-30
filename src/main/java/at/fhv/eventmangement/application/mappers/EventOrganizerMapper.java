package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.EventOrganizerDto;
import at.fhv.eventmangement.domain.model.EventOrganizer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ContactInfoMapper.class})
public interface EventOrganizerMapper {
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "created", ignore = true)
    EventOrganizer fromDto(EventOrganizerDto eventOrganizerDto);
    EventOrganizerDto toDto(EventOrganizer eventOrganizer);
}
