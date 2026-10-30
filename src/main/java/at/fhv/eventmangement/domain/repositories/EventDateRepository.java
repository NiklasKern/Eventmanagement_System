package at.fhv.eventmangement.domain.repositories;

import at.fhv.eventmangement.domain.model.EventDate;

import java.util.Optional;
import java.util.UUID;

public interface EventDateRepository {
    EventDate save(EventDate eventDate);
    Optional<EventDate> findById(UUID id);
}
