package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.form.UserAccountForm;
import at.fhv.eventmangement.application.mappers.UserAccountMapper;
import at.fhv.eventmangement.application.services.PasswordHasher;
import at.fhv.eventmangement.domain.exceptions.ProtectedRoleException;
import at.fhv.eventmangement.domain.exceptions.UserAccountAlreadyExistsException;
import at.fhv.eventmangement.domain.model.Role;
import at.fhv.eventmangement.domain.model.UserAccount;
import at.fhv.eventmangement.domain.repositories.CustomerRepository;
import at.fhv.eventmangement.domain.repositories.EventOrganizerRepository;
import at.fhv.eventmangement.domain.repositories.RoleRepository;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceImplTest {

    @Mock private UserAccountRepository userAccountRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private EventOrganizerRepository eventOrganizerRepository;
    @Mock private PasswordHasher passwordHasher;
    @Mock private UserAccountMapper userAccountMapper;

    @InjectMocks
    private UserAccountServiceImpl service;

    private static Role role(String name) {
        return Role.builder().id(UUID.randomUUID()).name(name).build();
    }

    private static UserAccount account(Role role) {
        return UserAccount.builder().id(UUID.randomUUID()).username("someone").enabled(true).role(role).build();
    }

    private static UserAccountForm form(String username, UUID roleId) {
        return UserAccountForm.builder()
                .username(username).password("Secret#2026").confirmPassword("Secret#2026").roleId(roleId).build();
    }

    @Test
    void createAccount_storesOnlyTheHash_andLowercaseUsername() throws Exception {
        Role customer = role(Role.CUSTOMER);
        when(roleRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(passwordHasher.hash("Secret#2026")).thenReturn("$argon2id$fake");
        when(userAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.createAccount(form("  Anna ", customer.getId()));

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("anna");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("$argon2id$fake");
        assertThat(captor.getValue().isEnabled()).isTrue();
        assertThat(captor.getValue().getRole()).isSameAs(customer);
    }

    @Test
    void createAccount_rejectsTakenUsername() {
        when(userAccountRepository.existsByUsernameIgnoreCase("anna")).thenReturn(true);

        assertThatThrownBy(() -> service.createAccount(form("Anna", UUID.randomUUID())))
                .isInstanceOf(UserAccountAlreadyExistsException.class);
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void disable_refusesTheLastEnabledAdmin() {
        UserAccount admin = account(role(Role.ADMIN));
        when(userAccountRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(userAccountRepository.countEnabledByRoleId(admin.getRole().getId())).thenReturn(1L);

        assertThatThrownBy(() -> service.disable(admin.getId()))
                .isInstanceOf(ProtectedRoleException.class);
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void disable_allowsAnAdminWhenAnotherOneRemains() throws Exception {
        UserAccount admin = account(role(Role.ADMIN));
        when(userAccountRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(userAccountRepository.countEnabledByRoleId(admin.getRole().getId())).thenReturn(2L);

        service.disable(admin.getId());

        assertThat(admin.isEnabled()).isFalse();
        verify(userAccountRepository).save(admin);
    }

    @Test
    void changeRole_refusesToDemoteTheLastEnabledAdmin() {
        UserAccount admin = account(role(Role.ADMIN));
        Role customer = role(Role.CUSTOMER);
        when(userAccountRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(roleRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(userAccountRepository.countEnabledByRoleId(admin.getRole().getId())).thenReturn(1L);

        assertThatThrownBy(() -> service.changeRole(admin.getId(), customer.getId()))
                .isInstanceOf(ProtectedRoleException.class);
    }

    @Test
    void disable_normalAccount_needsNoAdminCheck() throws Exception {
        UserAccount anna = account(role(Role.CUSTOMER));
        when(userAccountRepository.findById(anna.getId())).thenReturn(Optional.of(anna));

        service.disable(anna.getId());

        assertThat(anna.isEnabled()).isFalse();
    }
}