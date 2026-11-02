package at.fhv.eventmangement.infrastructure.mappers;

import at.fhv.eventmangement.domain.model.EventOrganizer;
import at.fhv.eventmangement.infrastructure.entities.EventOrganizerEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventOrganizerEntityMapper {
    EventOrganizer toDomain(EventOrganizerEntity entity);
    EventOrganizerEntity toEntity(EventOrganizer domain);
}
