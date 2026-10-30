package at.fhv.eventmangement.infrastructure.mappers;

import at.fhv.eventmangement.domain.model.Event;
import at.fhv.eventmangement.infrastructure.entities.EventEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",uses = {EventOrganizerEntityMapper.class, EventOrganizerEntityMapper.class})
public interface EventEntityMapper {
    Event toDomain(EventEntity entity);

    // Ignored because else it would create a new EventCategoryEntity object that isnt known to Hibernate
    @Mapping(target = "eventCategory", ignore = true)
    @Mapping(target = "eventOrganizer", ignore = true)
    EventEntity toEntity(Event domain);
}
