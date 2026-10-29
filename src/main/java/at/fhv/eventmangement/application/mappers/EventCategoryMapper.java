package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.EventCategoryDto;
import at.fhv.eventmangement.domain.model.EventCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventCategoryMapper {
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "created", ignore = true)
    EventCategory fromDto(EventCategoryDto eventCategoryDto);
    EventCategoryDto toDto(EventCategory eventCategory);
}
