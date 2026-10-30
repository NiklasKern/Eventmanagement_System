package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.infrastructure.entities.EventCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventCategoryJpaRepository extends JpaRepository<EventCategoryEntity, UUID> {
    boolean existsByCategoryNameIgnoreCase(String categoryName);
}
