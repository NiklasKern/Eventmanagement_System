package at.fhv.eventmangement.services.impl;

import at.fhv.eventmangement.domain.entities.EventCategory;
import at.fhv.eventmangement.repositories.EventCategoryRepository;
import at.fhv.eventmangement.services.EventCategoryService;
import jdk.jfr.Category;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
    public EventCategory addCategory(String name) {

        LocalDateTime now = LocalDateTime.now();

        return eventCategoryRepository.save(new EventCategory(
                null,
                name,
                now,
                now
        ));
    }
}
