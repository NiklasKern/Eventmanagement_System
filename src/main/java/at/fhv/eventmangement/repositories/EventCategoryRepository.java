package at.fhv.eventmangement.repositories;

import at.fhv.eventmangement.domain.entities.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventCategoryRepository extends JpaRepository<EventCategory, UUID> {
    boolean existsByCategoryNameIgnoreCase(String categoryName);
}
