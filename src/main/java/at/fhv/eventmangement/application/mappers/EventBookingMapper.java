package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.EventBookingDto;
import at.fhv.eventmangement.domain.model.EventBooking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CustomerMapper.class, EventDateMapper.class,EventParticipantMapper.class})
public interface EventBookingMapper {
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "created", ignore = true)
    EventBooking fromDto(EventBookingDto eventBookingDto);
    EventBookingDto toDto(EventBooking eventBooking);
}
