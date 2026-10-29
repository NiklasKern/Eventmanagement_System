package at.fhv.eventmangement.mappers;

import at.fhv.eventmangement.domain.dto.EventOrganizerDto;
import at.fhv.eventmangement.domain.entities.EventOrganizer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventOrganizerMapper {
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "created", ignore = true)
    EventOrganizer fromDto(EventOrganizerDto eventOrganizerDto);
    EventOrganizerDto toDto(EventOrganizer eventOrganizer);
}
