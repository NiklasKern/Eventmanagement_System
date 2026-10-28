package at.fhv.eventmangement.mappers.impl;

import at.fhv.eventmangement.domain.dto.EventCategoryDto;
import at.fhv.eventmangement.domain.entities.EventCategory;
import at.fhv.eventmangement.mappers.EventCategoryMapper;
import org.springframework.stereotype.Component;

@Component
public class EventCategoryMapperImpl implements EventCategoryMapper {

    @Override
    public EventCategory fromDto(EventCategoryDto eventCategoryDto) {
        return new EventCategory(
                eventCategoryDto.id(),
                eventCategoryDto.categoryName(),
                null,
                null
        );
    }

    @Override
    public EventCategoryDto toDto(EventCategory eventCategory) {
        return new EventCategoryDto(
                eventCategory.getId(),
                eventCategory.getCategoryName()
        );
    }
}
