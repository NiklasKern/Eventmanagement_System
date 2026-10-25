package at.fhv.eventmangement.mappers;

import at.fhv.eventmangement.domain.dto.EventDto;
import at.fhv.eventmangement.domain.entities.Event;


public interface EventMapper {

    EventDto toDto(Event event);

    Event fromDto(EventDto eventDto);
}
