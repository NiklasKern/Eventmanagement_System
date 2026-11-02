package at.fhv.eventmangement.domain.repositories;

import at.fhv.eventmangement.domain.model.EventOrganizer;

import java.util.Optional;

public interface EventOrganizerRepository {
    EventOrganizer save(EventOrganizer eventOrganizer);
    Optional<EventOrganizer> findByOrganizerName(String organizerName);
    boolean existsByOrganizerNameIgnoreCase(String organizerName);
}
