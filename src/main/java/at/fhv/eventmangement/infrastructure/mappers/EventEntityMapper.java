package at.fhv.eventmangement.infrastructure.mappers;

import at.fhv.eventmangement.domain.model.Event;
import at.fhv.eventmangement.domain.model.EventDate;
import at.fhv.eventmangement.infrastructure.entities.EventDateEntity;
import at.fhv.eventmangement.infrastructure.entities.EventEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",uses = {EventOrganizerEntityMapper.class, EventCategoryEntityMapper.class})
public interface EventEntityMapper {
    Event toDomain(EventEntity entity);

    @Mapping(target = "eventCategory", ignore = true)
    @Mapping(target = "eventOrganizer", ignore = true)
    EventEntity toEntity(Event domain);

    @Mapping(target = "event", ignore = true)
    EventDate toDomain(EventDateEntity entity);

    @Mapping(target = "event", ignore = true)
    EventDateEntity toEntity(EventDate domain);

    @AfterMapping
    default void linkEventDates(@MappingTarget EventEntity event) {
        if (event.getEventDates() != null) {
            event.getEventDates().forEach(date -> date.setEvent(event));
        }
    }
}
