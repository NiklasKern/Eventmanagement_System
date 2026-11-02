package at.fhv.eventmangement.domain.repositories;

import at.fhv.eventmangement.domain.model.EventCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventCategoryRepository {
    List<EventCategory> findAll();
    Optional<EventCategory> findById(UUID id);
    EventCategory save(EventCategory eventCategory);
    boolean existsByCategoryNameIgnoreCase(String categoryName);
}
