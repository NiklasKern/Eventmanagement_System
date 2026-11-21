package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.form.UserAccountForm;
import at.fhv.eventmangement.application.services.UserAccountService;
import at.fhv.eventmangement.domain.model.Permissions;
import at.fhv.eventmangement.domain.model.Role;
import at.fhv.eventmangement.domain.repositories.RoleRepository;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecuritySetupServiceImplTest {

    @Mock private RoleRepository roleRepository;
    @Mock private UserAccountRepository userAccountRepository;
    @Mock private UserAccountService userAccountService;

    @InjectMocks
    private SecuritySetupServiceImpl service;

    private final Role adminRole = Role.builder().id(UUID.randomUUID()).name(Role.ADMIN).build();

    @Test
    void ensureBuiltInRoles_createsAllSevenAsBuiltIn() {
        service.ensureBuiltInRoles();

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, times(7)).save(captor.capture());
        List<Role> created = captor.getAllValues();

        assertThat(created).extracting(Role::getName).containsExactlyInAnyOrder(
                Role.ADMIN, Role.CUSTOMER, Role.EVENT_PROVIDER, Role.EVENT_MANAGEMENT,
                Role.CUSTOMER_MANAGEMENT, Role.ON_PREM_CUSTOMER_MANAGEMENT, Role.FINANCE_DEPARTMENT);
        assertThat(created).allMatch(Role::isBuiltIn);
        assertThat(created).filteredOn(Role::isAdmin).singleElement()
                .satisfies(admin -> assertThat(admin.getPermissions()).containsExactlyInAnyOrder(Permissions.values()));
    }

    @Test
    void ensureBuiltInRoles_leavesExistingRolesAlone() {
        when(roleRepository.existsByNameIgnoreCase(any())).thenReturn(true);

        service.ensureBuiltInRoles();

        verify(roleRepository, never()).save(any());
    }

    @Test
    void ensureAdminAccount_doesNothingWhenAnEnabledAdminExists() throws Exception {
        when(roleRepository.findByName(Role.ADMIN)).thenReturn(Optional.of(adminRole));
        when(userAccountRepository.countEnabledByRoleId(adminRole.getId())).thenReturn(1L);

        service.ensureAdminAccount("admin", "Admin#2026");

        verifyNoInteractions(userAccountService);
    }

    @Test
    void ensureAdminAccount_createsAdminWhenNoneExists() throws Exception {
        when(roleRepository.findByName(Role.ADMIN)).thenReturn(Optional.of(adminRole));
        when(userAccountRepository.countEnabledByRoleId(adminRole.getId())).thenReturn(0L);

        service.ensureAdminAccount("admin", "Admin#2026");

        ArgumentCaptor<UserAccountForm> captor = ArgumentCaptor.forClass(UserAccountForm.class);
        verify(userAccountService).createAccount(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("admin");
        assertThat(captor.getValue().getPassword()).isEqualTo("Admin#2026");
        assertThat(captor.getValue().getRoleId()).isEqualTo(adminRole.getId());
    }

    @Test
    void ensureAdminAccount_failsWithClearMessageWhenNoPasswordIsConfigured() {
        when(roleRepository.findByName(Role.ADMIN)).thenReturn(Optional.of(adminRole));
        when(userAccountRepository.countEnabledByRoleId(adminRole.getId())).thenReturn(0L);

        assertThatThrownBy(() -> service.ensureAdminAccount("admin", ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.security.admin.password");
    }
}