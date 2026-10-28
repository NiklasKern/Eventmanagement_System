package at.fhv.eventmangement.mappers.impl;

import at.fhv.eventmangement.domain.dto.EventDto;
import at.fhv.eventmangement.domain.entities.Event;
import at.fhv.eventmangement.mappers.EventCategoryMapper;
import at.fhv.eventmangement.mappers.EventMapper;
import org.springframework.stereotype.Component;

@Component
public class EventMapperImpl implements EventMapper {

    private final EventCategoryMapper eventCategoryMapper;

    public EventMapperImpl(EventCategoryMapper eventCategoryMapper) {
        this.eventCategoryMapper = eventCategoryMapper;
    }

    @Override
    public EventDto toDto(Event event) {
        return new EventDto(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getLocation(),
                event.getDateTime(),
                event.getPrice(),
                event.getCapacity(),
                event.getEventStatus(),
                eventCategoryMapper.toDto(event.getEventCategory()),
                event.getEvent_organizer()
        );
    }

    @Override
    public Event fromDto(EventDto eventDto) {
        return new Event(
                eventDto.id(),
                eventDto.name(),
                eventDto.description(),
                eventDto.location(),
                eventDto.dateTime(),
                eventDto.price(),
                eventDto.capacity(),
                eventDto.eventStatus(),
                eventCategoryMapper.fromDto(eventDto.eventCategory()),
                eventDto.eventOrganizer(),
                null,
                null
        );
    }
}
