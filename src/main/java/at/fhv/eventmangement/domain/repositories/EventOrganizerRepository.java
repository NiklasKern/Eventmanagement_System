package at.fhv.eventmangement.domain.repositories;

import at.fhv.eventmangement.domain.model.EventOrganizer;

import java.util.Optional;
import java.util.UUID;

public interface EventOrganizerRepository {
    EventOrganizer save(EventOrganizer eventOrganizer);
    Optional<EventOrganizer> findByContactInfoName(String name);
    Optional<EventOrganizer> findById(UUID id);
    boolean existsByContactInfoNameIgnoreCase(String name);
}
