package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.infrastructure.entities.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, UUID> {
    Optional<UserAccountEntity> findByUsername(String name);
    boolean existsByUsernameIgnoreCase(String name);

    long countByRoleId(UUID roleId);
    long countByRoleIdAndEnabledTrue(UUID roleId);
}
