package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.Role;
import at.fhv.eventmangement.domain.repositories.RoleRepository;
import at.fhv.eventmangement.infrastructure.mappers.RoleEntityMapper;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class RoleRepositoryAdapter implements RoleRepository {
    private final RoleJpaRepository jpa;
    private final RoleEntityMapper mapper;

    public RoleRepositoryAdapter(RoleJpaRepository jpa,
                                 RoleEntityMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Role save(Role role) {
        return mapper.toDomain(jpa.save(mapper.toEntity(role)));
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return jpa.findByName(name).map(mapper::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return jpa.findAll(Sort.by("name")).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return jpa.existsByNameIgnoreCase(name);
    }
}
