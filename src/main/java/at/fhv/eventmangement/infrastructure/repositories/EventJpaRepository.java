package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventJpaRepository extends JpaRepository<Event, UUID> {
    boolean existsByName(String name);
}
