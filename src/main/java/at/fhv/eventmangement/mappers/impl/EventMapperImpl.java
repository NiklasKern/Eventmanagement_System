package at.fhv.eventmangement.mappers.impl;

import at.fhv.eventmangement.domain.dto.EventDto;
import at.fhv.eventmangement.domain.entities.Event;
import at.fhv.eventmangement.mappers.EventMapper;
import org.springframework.stereotype.Component;

@Component
public class EventMapperImpl implements EventMapper {
    @Override
    public EventDto toDto(Event event) {
        return new EventDto(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getDateTime(),
                event.getPrice(),
                event.getCapacity(),
                event.getEventStatus(),
                event.getEventCategory()
        );
    }

    @Override
    public Event fromDto(EventDto eventDto) {
        return new Event(
                eventDto.id(),
                eventDto.name(),
                eventDto.description(),
                eventDto.dateTime(),
                eventDto.price(),
                eventDto.capacity(),
                eventDto.eventStatus(),
                eventDto.eventCategory(),
                null,
                null
        );
    }
}
