package at.fhv.eventmangement.mappers;

import at.fhv.eventmangement.domain.dto.EventDto;
import at.fhv.eventmangement.domain.entities.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EventCategoryMapper.class, EventOrganizerMapper.class})
public interface EventMapper {
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "created", ignore = true)
    Event fromDto(EventDto eventDto);
    EventDto toDto(Event event);
}
