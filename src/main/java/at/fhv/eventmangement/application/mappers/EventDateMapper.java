package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.EventDateDto;
import at.fhv.eventmangement.domain.model.EventDate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventDateMapper {
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "event", ignore = true)
    EventDate fromDto(EventDateDto eventDateDto);
    EventDateDto toDto(EventDate eventDate);
}
