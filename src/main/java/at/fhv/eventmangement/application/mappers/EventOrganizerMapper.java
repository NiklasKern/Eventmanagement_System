package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.EventOrganizerDto;
import at.fhv.eventmangement.domain.model.EventOrganizer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ContactInfoMapper.class})
public interface EventOrganizerMapper {
    EventOrganizerDto toDto(EventOrganizer eventOrganizer);
}
