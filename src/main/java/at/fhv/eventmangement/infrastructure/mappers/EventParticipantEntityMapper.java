package at.fhv.eventmangement.infrastructure.mappers;

import at.fhv.eventmangement.domain.model.EventParticipant;
import at.fhv.eventmangement.infrastructure.entities.EventParticipantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CustomerEntityMapper.class})
public interface EventParticipantEntityMapper {
    @Mapping(target = "eventBooking", ignore = true)
    EventParticipant toDomain(EventParticipantEntity entity);

    @Mapping(target = "eventBooking", ignore = true)
    EventParticipantEntity toEntity(EventParticipant domain);
}
