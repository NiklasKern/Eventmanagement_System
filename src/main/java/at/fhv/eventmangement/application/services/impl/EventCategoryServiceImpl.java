package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.dto.EventCategoryDto;
import at.fhv.eventmangement.application.mappers.EventCategoryMapper;
import at.fhv.eventmangement.application.form.EventCategoryForm;
import at.fhv.eventmangement.domain.model.EventCategory;
import at.fhv.eventmangement.domain.exceptions.ValidationException;
import at.fhv.eventmangement.domain.repositories.EventCategoryRepository;
import at.fhv.eventmangement.application.services.EventCategoryService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EventCategoryServiceImpl implements EventCategoryService {

    private final EventCategoryRepository eventCategoryRepository;
    private final EventCategoryMapper eventCategoryMapper;

    public EventCategoryServiceImpl(EventCategoryRepository eventCategoryRepository, EventCategoryMapper eventCategoryMapper) {
        this.eventCategoryRepository = eventCategoryRepository;
        this.eventCategoryMapper = eventCategoryMapper;
    }

    @Override
    public List<EventCategoryDto> getAllEventCategories() {
        return eventCategoryRepository.findAll().stream().map(eventCategoryMapper::toDto).toList();
    }

    @Override
    @Transactional
    public void addCategory(EventCategoryForm form) {

        String name = form.getCategoryName().trim();

        if (eventCategoryRepository.existsByCategoryNameIgnoreCase(name)) {
            throw new ValidationException(
                    Map.of("categoryName", "category already exists"));
        }

        eventCategoryRepository.save(EventCategory.builder()
                .categoryName(name)
                .build()
        );
    }

}
