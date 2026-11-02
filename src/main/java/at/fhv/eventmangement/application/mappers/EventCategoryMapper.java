package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.EventCategoryDto;
import at.fhv.eventmangement.domain.model.EventCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventCategoryMapper {
    EventCategoryDto toDto(EventCategory eventCategory);
}
