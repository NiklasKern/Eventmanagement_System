package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.RoleDto;
import at.fhv.eventmangement.application.form.RoleForm;
import at.fhv.eventmangement.application.services.RoleService;
import at.fhv.eventmangement.domain.exceptions.ProtectedRoleException;
import at.fhv.eventmangement.domain.exceptions.RoleAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.RoleInUseException;
import at.fhv.eventmangement.domain.model.Permissions;
import at.fhv.eventmangement.presentation.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
@Import(SecurityConfig.class)
@WithMockUser(authorities = "MANAGE_ROLES")
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    private final UUID adminId = UUID.randomUUID();
    private final RoleDto admin = new RoleDto(adminId, "Admin", "Everything",
            EnumSet.allOf(Permissions.class), true);

    @BeforeEach
    void setUp() {
        when(roleService.getAllPermissions()).thenReturn(List.of(Permissions.values()));
    }

    @Test
    void list_showsRolesWithBuiltInBadge() throws Exception {
        when(roleService.getAllRoles()).thenReturn(List.of(admin));

        mockMvc.perform(get("/admin/roles"))
                .andExpect(status().isOk())
                .andExpect(view().name("role_list"))
                .andExpect(content().string(containsString("Admin")))
                .andExpect(content().string(containsString("built-in")));
    }

    @Test
    void createForm_showsEveryPermissionAsCheckbox() throws Exception {
        mockMvc.perform(get("/admin/roles/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("EVENT_VIEW")))
                .andExpect(content().string(containsString("MANAGE_ROLES")));
    }

    @Test
    void create_valid_savesTickedPermissionsAndRedirects() throws Exception {
        mockMvc.perform(post("/admin/roles/new").with(csrf())
                        .param("name", "Guide")
                        .param("permissions", "EVENT_VIEW", "BOOK_EVENTS"))
                .andExpect(redirectedUrl("/admin/roles"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(roleService).createRole(argThat((RoleForm f) -> f.getName().equals("Guide")
                && f.getPermissions().equals(Set.of(Permissions.EVENT_VIEW, Permissions.BOOK_EVENTS))));
    }

    @Test
    void create_withoutName_showsFormAgain() throws Exception {
        mockMvc.perform(post("/admin/roles/new").with(csrf()).param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("role_form"))
                .andExpect(model().attributeHasFieldErrors("role", "name"));

        verify(roleService, never()).createRole(any());
    }

    @Test
    void create_duplicateName_showsErrorOnName() throws Exception {
        when(roleService.createRole(any())).thenThrow(new RoleAlreadyExistsException());

        mockMvc.perform(post("/admin/roles/new").with(csrf()).param("name", "Admin"))
                .andExpect(view().name("role_form"))
                .andExpect(model().attributeHasFieldErrors("role", "name"));
    }

    @Test
    void update_adminLosingManageRoles_showsProtectedMessage() throws Exception {
        when(roleService.getRole(adminId)).thenReturn(admin);
        when(roleService.updateRole(eq(adminId), any()))
                .thenThrow(new ProtectedRoleException("the Admin role must keep the permissions to manage roles and users"));

        mockMvc.perform(post("/admin/roles/{id}/edit", adminId).with(csrf())
                        .param("name", "Admin")
                        .param("permissions", "EVENT_VIEW"))
                .andExpect(status().isOk())
                .andExpect(view().name("role_form"))
                .andExpect(content().string(containsString("must keep the permissions")));
    }

    @Test
    void delete_roleInUse_redirectsWithError() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RoleInUseException()).when(roleService).deleteRole(id);

        mockMvc.perform(post("/admin/roles/{id}/delete", id).with(csrf()))
                .andExpect(redirectedUrl("/admin/roles"))
                .andExpect(flash().attribute("errorMessage", "This role is already in use"));
    }

    @Test
    @WithMockUser(authorities = "EVENT_VIEW")
    void withoutManageRoles_isForbidden() throws Exception {
        mockMvc.perform(get("/admin/roles")).andExpect(status().isForbidden());
    }
}