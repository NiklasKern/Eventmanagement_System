package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.TestcontainersConfiguration;
import at.fhv.eventmangement.domain.model.ContactInfo;
import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.model.CustomerType;
import at.fhv.eventmangement.domain.model.Permissions;
import at.fhv.eventmangement.domain.model.Role;
import at.fhv.eventmangement.domain.model.UserAccount;
import at.fhv.eventmangement.domain.repositories.CustomerRepository;
import at.fhv.eventmangement.domain.repositories.RoleRepository;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
public class RoleAndUserAccountRepositoryIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private EntityManager entityManager;

    // write to DB and forget cache so that only postgresql comes through in next read
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private Role saveRole(String name, Permissions... permissions) {
        return roleRepository.save(Role.builder()
                .name(name)
                .description("test role")
                .permissions(permissions.length == 0
                        ? EnumSet.noneOf(Permissions.class)
                        : EnumSet.of(permissions[0], permissions))
                .build());
    }

    private UserAccount newAccount(String username, Role role) {
        return UserAccount.builder()
                .username(username)
                .passwordHash("hash")
                .enabled(true)
                .role(role)
                .build();
    }

    @Test
    void role_isSavedWithItsPermissions() {
        Role saved = saveRole("Tester", Permissions.EVENT_VIEW, Permissions.BOOK_EVENTS);
        flushAndClear();

        assertThat(saved.getId()).isNotNull();
        assertThat(roleRepository.findByName("Tester"))
                .get()
                .satisfies(role -> {
                    assertThat(role.getPermissions())
                            .containsExactlyInAnyOrder(Permissions.EVENT_VIEW, Permissions.BOOK_EVENTS);
                    assertThat(role.getCreated()).isNotNull();
                });
    }

    @Test
    void role_changedPermissionsAreSaved() throws Exception {
        Role role = saveRole("Tester", Permissions.EVENT_VIEW);
        flushAndClear();

        role.changePermissions(EnumSet.of(Permissions.VIEW_PAYMENTS));
        roleRepository.save(role);
        flushAndClear();

        assertThat(roleRepository.findById(role.getId()).orElseThrow().getPermissions())
                .containsExactly(Permissions.VIEW_PAYMENTS);
    }

    @Test
    void role_withoutPermissions_canBeSaved() {
        Role saved = saveRole("Empty");
        flushAndClear();

        assertThat(roleRepository.findById(saved.getId()).orElseThrow().getPermissions()).isEmpty();
    }

    @Test
    void role_existsByNameIgnoresCase() {
        saveRole("Tester");

        assertThat(roleRepository.existsByNameIgnoreCase("tester")).isTrue();
        assertThat(roleRepository.existsByNameIgnoreCase("Other")).isFalse();
    }

    @Test
    void role_canBeDeleted() {
        Role saved = saveRole("Tester");
        flushAndClear();

        roleRepository.deleteById(saved.getId());
        flushAndClear();

        assertThat(roleRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void account_isSavedWithItsRole() {
        Role role = saveRole("Tester", Permissions.EVENT_VIEW);
        UserAccount saved = userAccountRepository.save(newAccount("anna", role));
        flushAndClear();

        assertThat(saved.getId()).isNotNull();
        assertThat(userAccountRepository.findByUsername("anna"))
                .get()
                .satisfies(account -> {
                    assertThat(account.getPasswordHash()).isEqualTo("hash");
                    assertThat(account.isEnabled()).isTrue();
                    assertThat(account.getRole().getName()).isEqualTo("Tester");
                    assertThat(account.hasPermission(Permissions.EVENT_VIEW)).isTrue();
                    assertThat(account.getCustomer()).isNull();
                });
    }

    @Test
    void account_isLinkedToItsCustomerProfile() {
        Customer customer = customerRepository.save(Customer.builder()
                .contactInfo(new ContactInfo("Anna", "Street 1", "anna@test.at"))
                .customerType(CustomerType.PERSON)
                .build());
        UserAccount account = newAccount("anna", saveRole("Tester"));
        UserAccount withCustomer = UserAccount.builder()
                .username(account.getUsername())
                .passwordHash(account.getPasswordHash())
                .enabled(true)
                .role(account.getRole())
                .customer(customer)
                .build();
        userAccountRepository.save(withCustomer);
        flushAndClear();

        assertThat(userAccountRepository.findByUsername("anna").orElseThrow().getCustomer().getContactInfo().getName())
                .isEqualTo("Anna");
    }

    @Test
    void account_changedRoleIsSaved() {
        Role first = saveRole("First");
        Role second = saveRole("Second");
        UserAccount account = userAccountRepository.save(newAccount("anna", first));
        flushAndClear();

        account.changeRole(second);
        userAccountRepository.save(account);
        flushAndClear();

        assertThat(userAccountRepository.findById(account.getId()).orElseThrow().getRole().getName())
                .isEqualTo("Second");
    }

    @Test
    void account_existsByUsernameIgnoresCase() {
        userAccountRepository.save(newAccount("anna", saveRole("Tester")));

        assertThat(userAccountRepository.existsByUsernameIgnoreCase("ANNA")).isTrue();
        assertThat(userAccountRepository.existsByUsernameIgnoreCase("ben")).isFalse();
    }

    @Test
    void countByRole_countsAllAccounts_andEnabledOnlyTheEnabledOnes() {
        Role role = saveRole("Tester");
        Role other = saveRole("Other");
        userAccountRepository.save(newAccount("anna", role));
        UserAccount ben = newAccount("ben", role);
        ben.disable();
        userAccountRepository.save(ben);
        userAccountRepository.save(newAccount("clara", other));

        assertThat(userAccountRepository.countByRoleId(role.getId())).isEqualTo(2);
        assertThat(userAccountRepository.countEnabledByRoleId(role.getId())).isEqualTo(1);
    }
}
