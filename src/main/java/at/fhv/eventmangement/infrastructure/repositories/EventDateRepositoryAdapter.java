package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.EventDate;
import at.fhv.eventmangement.domain.repositories.EventDateRepository;
import at.fhv.eventmangement.infrastructure.mappers.EventDateEntityMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class EventDateRepositoryAdapter implements EventDateRepository {

    private final EventDateJpaRepository jpa;
    private final EventDateEntityMapper mapper;

    public EventDateRepositoryAdapter(EventDateJpaRepository jpa, EventDateEntityMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public EventDate save(EventDate eventDate) {
        return mapper.toDomain(jpa.save(mapper.toEntity(eventDate)));
    }

    @Override
    public Optional<EventDate> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }
}
