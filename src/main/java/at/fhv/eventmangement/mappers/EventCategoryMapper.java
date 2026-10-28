package at.fhv.eventmangement.mappers;

import at.fhv.eventmangement.domain.dto.EventCategoryDto;
import at.fhv.eventmangement.domain.entities.EventCategory;

public interface EventCategoryMapper {

    public EventCategory fromDto(EventCategoryDto eventCategoryDto);
    public EventCategoryDto toDto(EventCategory eventCategory);
}
