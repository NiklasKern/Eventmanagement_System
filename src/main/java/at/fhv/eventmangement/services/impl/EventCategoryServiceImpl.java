package at.fhv.eventmangement.services.impl;

import at.fhv.eventmangement.domain.form.EventCategoryForm;
import at.fhv.eventmangement.domain.entities.EventCategory;
import at.fhv.eventmangement.exceptions.ValidationException;
import at.fhv.eventmangement.repositories.EventCategoryRepository;
import at.fhv.eventmangement.services.EventCategoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class EventCategoryServiceImpl implements EventCategoryService {

    private final EventCategoryRepository eventCategoryRepository;

    public EventCategoryServiceImpl(EventCategoryRepository eventCategoryRepository) {
        this.eventCategoryRepository = eventCategoryRepository;
    }

    @Override
    public List<EventCategory> getAllEventCategories() {
        return eventCategoryRepository.findAll();
    }

    @Override
    public EventCategory addCategory(EventCategoryForm form) {

        String name = form.getCategoryName().trim();

        if (eventCategoryRepository.existsByCategoryNameIgnoreCase(name)) {
            throw new ValidationException(
                    Map.of("categoryName", "category already exists"));
        }

        LocalDateTime now = LocalDateTime.now();

        return eventCategoryRepository.save(new EventCategory(
                null,
                form.getCategoryName().trim(),
                now,
                now
        ));
    }

}
