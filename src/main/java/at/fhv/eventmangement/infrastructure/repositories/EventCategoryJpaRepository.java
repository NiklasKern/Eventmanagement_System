package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.EventCategory;
import at.fhv.eventmangement.domain.repositories.EventCategoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventCategoryJpaRepository extends JpaRepository<EventCategory, UUID>, EventCategoryRepository {
}
