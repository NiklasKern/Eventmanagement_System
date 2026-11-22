package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.RoleDto;
import at.fhv.eventmangement.application.services.RoleService;
import at.fhv.eventmangement.domain.exceptions.ProtectedRoleException;
import at.fhv.eventmangement.domain.exceptions.RoleInUseException;
import at.fhv.eventmangement.domain.exceptions.RoleNotFoundException;
import at.fhv.eventmangement.domain.model.Permissions;
import at.fhv.eventmangement.presentation.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleRestController.class)
@Import(SecurityConfig.class)
@WithMockUser(authorities = "MANAGE_ROLES")
class RoleRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    private final UUID id = UUID.randomUUID();
    private final RoleDto guide = new RoleDto(id, "Guide", "Tour guides",
            EnumSet.of(Permissions.EVENT_VIEW), false);

    private static final String GUIDE_JSON = """
            {"name": "Guide", "description": "Tour guides", "permissions": ["EVENT_VIEW"]}
            """;

    @Test
    void getRoles_returnsJson() throws Exception {
        when(roleService.getAllRoles()).thenReturn(List.of(guide));

        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Guide"))
                .andExpect(jsonPath("$[0].permissions[0]").value("EVENT_VIEW"));
    }

    @Test
    void createRole_returns201WithLocation() throws Exception {
        when(roleService.createRole(any())).thenReturn(guide);

        mockMvc.perform(post("/api/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON).content(GUIDE_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/admin/roles/" + id))
                .andExpect(jsonPath("$.name").value("Guide"));
    }

    @Test
    void createRole_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest());

        verify(roleService, never()).createRole(any());
    }

    @Test
    void getRole_unknown_returns404() throws Exception {
        when(roleService.getRole(id)).thenThrow(new RoleNotFoundException());

        mockMvc.perform(get("/api/admin/roles/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRole_protected_returns409() throws Exception {
        when(roleService.updateRole(eq(id), any()))
                .thenThrow(new ProtectedRoleException("built-in roles cannot be changed"));

        mockMvc.perform(put("/api/admin/roles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(GUIDE_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("built-in roles cannot be changed"));
    }

    @Test
    void deleteRole_inUse_returns409() throws Exception {
        doThrow(new RoleInUseException()).when(roleService).deleteRole(id);

        mockMvc.perform(delete("/api/admin/roles/{id}", id))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteRole_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/roles/{id}", id))
                .andExpect(status().isNoContent());
    }
}