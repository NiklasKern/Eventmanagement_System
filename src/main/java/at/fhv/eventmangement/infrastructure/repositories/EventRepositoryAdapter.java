package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.Event;
import at.fhv.eventmangement.domain.repositories.EventRepository;
import at.fhv.eventmangement.infrastructure.entities.EventEntity;
import at.fhv.eventmangement.infrastructure.mappers.EventEntityMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class EventRepositoryAdapter implements EventRepository {

    private final EventJpaRepository jpa;
    private final EventCategoryJpaRepository categoryJpa;
    private final EventOrganizerJpaRepository organizerJpa;
    private final EventEntityMapper mapper;

    public EventRepositoryAdapter(EventJpaRepository jpa,
                                  EventCategoryJpaRepository categoryJpa,
                                  EventOrganizerJpaRepository organizerJpa,
                                  EventEntityMapper mapper) {
        this.jpa = jpa;
        this.categoryJpa = categoryJpa;
        this.organizerJpa = organizerJpa;
        this.mapper = mapper;
    }

    @Override
    public List<Event> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return jpa.existsByName(name);
    }

    @Override
    @Transactional
    public Event save(Event event) {
        EventEntity entity = mapper.toEntity(event);

        entity.setEventCategory(categoryJpa.getReferenceById(event.getEventCategory().getId()));
        entity.setEventOrganizer(organizerJpa.getReferenceById(event.getEventOrganizer().getId()));
        return mapper.toDomain(jpa.save(entity));
    }
}
