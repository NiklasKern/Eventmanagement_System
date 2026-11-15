package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.EventStatus;
import at.fhv.eventmangement.infrastructure.entities.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventJpaRepository extends JpaRepository<EventEntity, UUID>, JpaSpecificationExecutor<EventEntity> {
    boolean existsByName(String name);
    List<EventEntity> findByEventStatus(EventStatus eventStatus);

    @Query("select distinct e.location from EventEntity e where e.eventStatus = :status order by e.location")
    List<String> findLocations(@Param("status") EventStatus status);
}
