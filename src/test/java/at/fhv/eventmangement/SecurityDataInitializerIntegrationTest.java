package at.fhv.eventmangement;

import at.fhv.eventmangement.application.services.PasswordHasher;
import at.fhv.eventmangement.domain.model.Role;
import at.fhv.eventmangement.domain.model.UserAccount;
import at.fhv.eventmangement.domain.repositories.RoleRepository;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

// Starts the whole app (needs Docker) and checks the seed data that the initializer created
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class SecurityDataInitializerIntegrationTest {

    @Autowired private RoleRepository roleRepository;
    @Autowired private UserAccountRepository userAccountRepository;
    @Autowired private PasswordHasher passwordHasher;

    @Test
    void allSevenBuiltInRolesExist() {
        assertThat(roleRepository.findAll())
                .filteredOn(Role::isBuiltIn)
                .extracting(Role::getName)
                .containsExactlyInAnyOrder(
                        Role.ADMIN, Role.CUSTOMER, Role.EVENT_PROVIDER, Role.EVENT_MANAGEMENT,
                        Role.CUSTOMER_MANAGEMENT, Role.ON_PREM_CUSTOMER_MANAGEMENT, Role.FINANCE_DEPARTMENT);
    }

    @Test
    void adminAccountExists_withHashedConfiguredPassword() {
        UserAccount admin = userAccountRepository.findByUsername("admin").orElseThrow();

        assertThat(admin.getRole().isAdmin()).isTrue();
        assertThat(admin.isEnabled()).isTrue();
        assertThat(admin.getPasswordHash()).startsWith("$argon2id$");
        assertThat(passwordHasher.matches("Test#Admin2026", admin.getPasswordHash())).isTrue();
    }
}