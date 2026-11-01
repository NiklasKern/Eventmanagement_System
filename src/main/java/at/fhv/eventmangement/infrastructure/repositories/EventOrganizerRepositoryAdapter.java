package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.EventOrganizer;
import at.fhv.eventmangement.domain.repositories.EventOrganizerRepository;
import at.fhv.eventmangement.infrastructure.mappers.EventOrganizerEntityMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class EventOrganizerRepositoryAdapter implements EventOrganizerRepository {

    private final EventOrganizerJpaRepository jpa;
    private final EventOrganizerEntityMapper mapper;

    public EventOrganizerRepositoryAdapter(EventOrganizerJpaRepository jpa,
                                           EventOrganizerEntityMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public EventOrganizer save(EventOrganizer organizer) {
        return mapper.toDomain(jpa.save(mapper.toEntity(organizer)));
    }

    @Override
    public Optional<EventOrganizer> findByContactInfoName(String name) {
        return jpa.findByContactInfoName(name).map(mapper::toDomain);
    }

    @Override
    public boolean existsByContactInfoNameIgnoreCase(String name) {
        return jpa.existsByContactInfoNameIgnoreCase(name);
    }
}
