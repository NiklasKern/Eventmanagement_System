package at.fhv.eventmangement.domain.repositories;

import at.fhv.eventmangement.domain.model.EventOrganizer;

import java.util.Optional;

public interface EventOrganizerRepository {
    EventOrganizer save(EventOrganizer eventOrganizer);
    Optional<EventOrganizer> findByContactInfoName(String name);
    boolean existsByContactInfoNameIgnoreCase(String name);
}
