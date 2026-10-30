package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.EventParticipantDto;
import at.fhv.eventmangement.domain.model.EventParticipant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CustomerMapper.class})
public interface EventParticipantMapper {
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "eventBooking", ignore = true)
    EventParticipant fromDto(EventParticipantDto eventParticipantDto);
    EventParticipantDto toDto(EventParticipant eventParticipant);
}
