package at.fhv.eventmangement.domain.repositories;

import at.fhv.eventmangement.domain.model.Event;
import at.fhv.eventmangement.domain.model.EventStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {
    List<Event> findAll();
    List<Event> findByEventStatus(EventStatus eventStatus);
    Event save(Event event);
    boolean existsByName(String name);
    Optional<Event> findById(UUID id);
}
