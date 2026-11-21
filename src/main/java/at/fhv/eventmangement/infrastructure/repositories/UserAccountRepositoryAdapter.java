package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.UserAccount;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import at.fhv.eventmangement.infrastructure.entities.UserAccountEntity;
import at.fhv.eventmangement.infrastructure.mappers.UserAccountEntityMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class UserAccountRepositoryAdapter implements UserAccountRepository {
    private final UserAccountJpaRepository jpa;
    private final UserAccountEntityMapper mapper;
    private final RoleJpaRepository roleJpa;
    private final CustomerJpaRepository customerJpa;
    private final EventOrganizerJpaRepository organizerJpa;

    public UserAccountRepositoryAdapter(UserAccountJpaRepository jpa, UserAccountEntityMapper mapper,
                                        RoleJpaRepository roleJpa, CustomerJpaRepository customerJpa,
                                        EventOrganizerJpaRepository organizerJpa) {
        this.jpa = jpa;
        this.mapper = mapper;
        this.roleJpa = roleJpa;
        this.customerJpa = customerJpa;
        this.organizerJpa = organizerJpa;
    }

    @Override
    @Transactional
    public UserAccount save(UserAccount account) {
        UserAccountEntity entity = mapper.toEntity(account);

        entity.setRole(roleJpa.getReferenceById(account.getRole().getId()));
        if (account.getCustomer() != null) {
            entity.setCustomer(customerJpa.getReferenceById(account.getCustomer().getId()));
        }
        if (account.getOrganizer() != null) {
            entity.setOrganizer(organizerJpa.getReferenceById(account.getOrganizer().getId()));
        }

        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<UserAccount> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return jpa.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public List<UserAccount> findAll() {
        return jpa.findAll(Sort.by("username")).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByUsernameIgnoreCase(String username) {
        return jpa.existsByUsernameIgnoreCase(username);
    }

    @Override
    public long countByRoleId(UUID roleId) {
        return jpa.countByRoleId(roleId);
    }

    @Override
    public long countEnabledByRoleId(UUID roleId) {
        return jpa.countByRoleIdAndEnabledTrue(roleId);
    }
}
