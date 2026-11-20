package at.fhv.eventmangement.domain.repositories;

import at.fhv.eventmangement.domain.model.UserAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository {
    UserAccount save(UserAccount account);
    Optional<UserAccount> findById(UUID id);
    Optional<UserAccount> findByUsername(String username);
    List<UserAccount> findAll();
    boolean existsByUsernameIgnoreCase(String username);

    long countByRoleId(UUID roleId);
    long countEnabledByRoleId(UUID roleId);
}