package at.fhv.eventmangement.infrastructure.mappers;

import at.fhv.eventmangement.domain.model.EventCategory;
import at.fhv.eventmangement.infrastructure.entities.EventCategoryEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventCategoryEntityMapper {
    EventCategory toDomain(EventCategoryEntity entity);
    EventCategoryEntity toEntity(EventCategory domain);
}
