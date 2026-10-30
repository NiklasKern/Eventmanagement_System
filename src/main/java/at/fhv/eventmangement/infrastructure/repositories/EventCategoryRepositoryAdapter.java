package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.EventCategory;
import at.fhv.eventmangement.domain.repositories.EventCategoryRepository;
import at.fhv.eventmangement.infrastructure.mappers.EventCategoryEntityMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class EventCategoryRepositoryAdapter implements EventCategoryRepository {

    private final EventCategoryJpaRepository jpa;
    private final EventCategoryEntityMapper mapper;

    public EventCategoryRepositoryAdapter(EventCategoryJpaRepository jpa,
                                          EventCategoryEntityMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public List<EventCategory> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<EventCategory> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public EventCategory save(EventCategory category) {
        return mapper.toDomain(jpa.save(mapper.toEntity(category)));
    }

    @Override
    public boolean existsByCategoryNameIgnoreCase(String categoryName) {
        return jpa.existsByCategoryNameIgnoreCase(categoryName);
    }
}
