package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.domain.model.Event;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {EventCategoryMapper.class, EventOrganizerMapper.class})
public interface EventMapper {
    EventDto toDto(Event event);
}
