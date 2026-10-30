package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.EventDate;
import at.fhv.eventmangement.domain.repositories.EventDateRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventDateJpaRepository extends JpaRepository<EventDate, UUID>, EventDateRepository {
}
