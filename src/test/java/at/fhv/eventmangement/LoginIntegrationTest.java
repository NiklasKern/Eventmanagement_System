package at.fhv.eventmangement;

import at.fhv.eventmangement.application.form.UserAccountForm;
import at.fhv.eventmangement.application.services.UserAccountService;
import at.fhv.eventmangement.domain.model.Role;
import at.fhv.eventmangement.domain.repositories.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Real login against db
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class LoginIntegrationTest {

    private static final String ADMIN_PASSWORD = "Test#Admin2026";   // from application-test.properties

    @Autowired private MockMvc mockMvc;
    @Autowired private UserAccountService userAccountService;
    @Autowired private RoleRepository roleRepository;

    @Test
    void pagesRequireLogin() throws Exception {
        mockMvc.perform(get("/events").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void apiWithoutLogin_returns401() throws Exception {
        mockMvc.perform(get("/api/events")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void adminCanLogIn_andLandsOnStartPage() throws Exception {
        mockMvc.perform(formLogin("/login").user("admin").password(ADMIN_PASSWORD))
                .andExpect(authenticated().withUsername("admin"))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void usernameIsCaseInsensitive() throws Exception {
        mockMvc.perform(formLogin("/login").user("ADMIN").password(ADMIN_PASSWORD))
                .andExpect(authenticated().withUsername("admin"));
    }

    @Test
    void wrongPassword_isRejected() throws Exception {
        mockMvc.perform(formLogin("/login").user("admin").password("Wrong#Pass1"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void disabledAccount_cannotLogIn() throws Exception {
        Role customer = roleRepository.findByName(Role.CUSTOMER).orElseThrow();
        var account = userAccountService.createAccount(UserAccountForm.builder()
                .username("blocked").password("Blocked#2026").confirmPassword("Blocked#2026")
                .roleId(customer.getId()).build());
        userAccountService.disable(account.id());

        mockMvc.perform(formLogin("/login").user("blocked").password("Blocked#2026"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void logout_endsTheSession() throws Exception {
        mockMvc.perform(logout())
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?logout"));
    }
}