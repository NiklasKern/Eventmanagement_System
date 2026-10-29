package at.fhv.eventmangement.domain.repositories;

import at.fhv.eventmangement.domain.model.Event;

import java.util.List;

public interface EventRepository {
    List<Event> findAll();
    Event save(Event event);
    boolean existsByName(String name);
}
