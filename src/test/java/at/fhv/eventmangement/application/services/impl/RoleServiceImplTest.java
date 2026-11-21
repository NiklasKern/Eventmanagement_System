package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.form.RoleForm;
import at.fhv.eventmangement.application.mappers.RoleMapper;
import at.fhv.eventmangement.domain.exceptions.ProtectedRoleException;
import at.fhv.eventmangement.domain.exceptions.RoleAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.RoleInUseException;
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

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock private RoleRepository roleRepository;
    @Mock private UserAccountRepository userAccountRepository;
    @Mock private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl service;

    @Test
    void createRole_savesTrimmedNameAndPermissions() throws Exception {
        when(roleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.createRole(new RoleForm("  Auditor ", "reads things", Set.of(Permissions.VIEW_ALL_BOOKINGS)));

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Auditor");
        assertThat(captor.getValue().getPermissions()).containsExactly(Permissions.VIEW_ALL_BOOKINGS);
        assertThat(captor.getValue().isBuiltIn()).isFalse();
    }

    @Test
    void createRole_rejectsDuplicateName() {
        when(roleRepository.existsByNameIgnoreCase("Auditor")).thenReturn(true);

        assertThatThrownBy(() -> service.createRole(new RoleForm("Auditor", null, Set.of())))
                .isInstanceOf(RoleAlreadyExistsException.class);
    }

    @Test
    void updateRole_adminMustKeepManagementPermissions() {
        Role admin = Role.builder().id(UUID.randomUUID()).name(Role.ADMIN).builtIn(true)
                .permissions(EnumSet.allOf(Permissions.class)).build();
        when(roleRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.updateRole(admin.getId(),
                new RoleForm(Role.ADMIN, null, Set.of(Permissions.EVENT_VIEW))))
                .isInstanceOf(ProtectedRoleException.class);
        verify(roleRepository, never()).save(any());
    }

    @Test
    void deleteRole_refusesBuiltInRoles() {
        Role customer = Role.builder().id(UUID.randomUUID()).name(Role.CUSTOMER).builtIn(true).build();
        when(roleRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> service.deleteRole(customer.getId()))
                .isInstanceOf(ProtectedRoleException.class);
        verify(roleRepository, never()).deleteById(any());
    }

    @Test
    void deleteRole_refusesRolesThatAreStillAssigned() {
        Role auditor = Role.builder().id(UUID.randomUUID()).name("Auditor").build();
        when(roleRepository.findById(auditor.getId())).thenReturn(Optional.of(auditor));
        when(userAccountRepository.countByRoleId(auditor.getId())).thenReturn(3L);

        assertThatThrownBy(() -> service.deleteRole(auditor.getId()))
                .isInstanceOf(RoleInUseException.class);
        verify(roleRepository, never()).deleteById(any());
    }

    @Test
    void deleteRole_deletesUnusedCustomRole() throws Exception {
        Role auditor = Role.builder().id(UUID.randomUUID()).name("Auditor").build();
        when(roleRepository.findById(auditor.getId())).thenReturn(Optional.of(auditor));
        when(userAccountRepository.countByRoleId(auditor.getId())).thenReturn(0L);

        service.deleteRole(auditor.getId());

        verify(roleRepository).deleteById(auditor.getId());
    }
}