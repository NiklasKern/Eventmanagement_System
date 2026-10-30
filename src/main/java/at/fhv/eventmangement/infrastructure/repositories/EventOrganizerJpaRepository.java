package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.EventOrganizer;
import at.fhv.eventmangement.infrastructure.entities.EventOrganizerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventOrganizerJpaRepository extends JpaRepository<EventOrganizer, UUID> {
    Optional<EventOrganizerEntity> findByOrganizerName(String organizerName);
    boolean existsByOrganizerNameIgnoreCase(String organizername);
}
