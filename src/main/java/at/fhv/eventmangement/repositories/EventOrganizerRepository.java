package at.fhv.eventmangement.repositories;

import at.fhv.eventmangement.domain.entities.EventOrganizer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventOrganizerRepository extends JpaRepository<EventOrganizer, UUID> {
    Optional<EventOrganizer> findByOrganizerName(String organizerName);
    boolean existsByOrganizerNameIgnoreCase(String organizerName);

}
