package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.Event;
import at.fhv.eventmangement.domain.model.EventStatus;
import at.fhv.eventmangement.infrastructure.entities.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventJpaRepository extends JpaRepository<EventEntity, UUID> {
    boolean existsByName(String name);
    List<EventEntity> findByEventStatus(EventStatus eventStatus);
}
