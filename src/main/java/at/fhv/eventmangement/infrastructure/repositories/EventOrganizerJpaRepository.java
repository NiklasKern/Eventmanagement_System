package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.EventOrganizer;
import at.fhv.eventmangement.domain.repositories.EventOrganizerRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventOrganizerJpaRepository extends JpaRepository<EventOrganizer, UUID>, EventOrganizerRepository {
}
