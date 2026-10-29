package at.fhv.eventmangement.mappers;

import at.fhv.eventmangement.domain.dto.EventCategoryDto;
import at.fhv.eventmangement.domain.entities.EventCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventCategoryMapper {
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "created", ignore = true)
    EventCategory fromDto(EventCategoryDto eventCategoryDto);
    EventCategoryDto toDto(EventCategory eventCategory);
}
