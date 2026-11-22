package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.UserAccountDto;
import at.fhv.eventmangement.application.services.CustomerService;
import at.fhv.eventmangement.application.services.EventOrganizerService;
import at.fhv.eventmangement.application.services.RoleService;
import at.fhv.eventmangement.application.services.UserAccountService;
import at.fhv.eventmangement.domain.exceptions.ProtectedRoleException;
import at.fhv.eventmangement.domain.exceptions.UserAccountAlreadyExistsException;
import at.fhv.eventmangement.presentation.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserAccountController.class)
@Import(SecurityConfig.class)
@WithMockUser(authorities = "MANAGE_USERS")
class UserAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserAccountService userAccountService;
    @MockitoBean
    private RoleService roleService;
    @MockitoBean
    private CustomerService customerService;
    @MockitoBean
    private EventOrganizerService eventOrganizerService;

    private final UUID accountId = UUID.randomUUID();
    private final UUID roleId = UUID.randomUUID();

    @Test
    void list_showsAccounts() throws Exception {
        when(userAccountService.getAllAccounts()).thenReturn(List.of(
                new UserAccountDto(accountId, "anna", true, roleId, "Customer", null, null)));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("user_list"))
                .andExpect(content().string(containsString("anna")));
    }

    @Test
    void create_valid_redirectsToList() throws Exception {
        mockMvc.perform(post("/admin/users/new").with(csrf())
                        .param("username", "anna")
                        .param("password", "Anna#2026x")
                        .param("confirmPassword", "Anna#2026x")
                        .param("roleId", roleId.toString()))
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userAccountService).createAccount(any());
    }

    @Test
    void create_weakPassword_showsErrorOnPassword() throws Exception {
        mockMvc.perform(post("/admin/users/new").with(csrf())
                        .param("username", "anna")
                        .param("password", "password")
                        .param("confirmPassword", "password")
                        .param("roleId", roleId.toString()))
                .andExpect(view().name("user_form"))
                .andExpect(model().attributeHasFieldErrors("account", "password"));

        verify(userAccountService, never()).createAccount(any());
    }

    @Test
    void create_passwordsDoNotMatch_showsErrorAndNeverEchoesPassword() throws Exception {
        mockMvc.perform(post("/admin/users/new").with(csrf())
                        .param("username", "anna")
                        .param("password", "Anna#2026x")
                        .param("confirmPassword", "Other#2026x")
                        .param("roleId", roleId.toString()))
                .andExpect(view().name("user_form"))
                .andExpect(model().attributeHasFieldErrors("account", "passwordConfirmed"))
                // the typed password must not appear in the HTML again
                .andExpect(content().string(not(containsString("Anna#2026x"))));
    }

    @Test
    void create_duplicateUsername_showsErrorOnUsername() throws Exception {
        when(userAccountService.createAccount(any())).thenThrow(new UserAccountAlreadyExistsException());

        mockMvc.perform(post("/admin/users/new").with(csrf())
                        .param("username", "admin")
                        .param("password", "Admin#2026x")
                        .param("confirmPassword", "Admin#2026x")
                        .param("roleId", roleId.toString()))
                .andExpect(view().name("user_form"))
                .andExpect(model().attributeHasFieldErrors("account", "username"));
    }

    @Test
    void changeRole_lastAdmin_redirectsWithError() throws Exception {
        when(userAccountService.changeRole(accountId, roleId))
                .thenThrow(new ProtectedRoleException("Last admin can not be disabled or given another role"));

        mockMvc.perform(post("/admin/users/{id}/role", accountId).with(csrf())
                        .param("roleId", roleId.toString()))
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attribute("errorMessage", "Last admin can not be disabled or given another role"));
    }

    @Test
    @WithMockUser(authorities = "MANAGE_ROLES")
    void withoutManageUsers_isForbidden() throws Exception {
        mockMvc.perform(get("/admin/users")).andExpect(status().isForbidden());
    }
}