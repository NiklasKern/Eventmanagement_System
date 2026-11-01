package at.fhv.eventmangement.infrastructure.mappers;

import at.fhv.eventmangement.domain.model.EventDate;
import at.fhv.eventmangement.infrastructure.entities.EventDateEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = EventEntityMapper.class)
public interface EventDateEntityMapper {
    EventDate toDomain(EventDateEntity entity);
    EventDateEntity toEntity(EventDate domain);
}