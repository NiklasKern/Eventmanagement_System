package at.fhv.eventmangement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Which permission may open which URL (rules from SecurityConfig)
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class AuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // --- customer: EVENT_VIEW + BOOK_EVENTS ---

    @Test
    @WithMockUser(authorities = {"EVENT_VIEW", "BOOK_EVENTS"})
    void customer_canSeeCatalog() throws Exception {
        mockMvc.perform(get("/customer/events")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"EVENT_VIEW", "BOOK_EVENTS"})
    void customer_cannotOpenOrganizerList() throws Exception {
        mockMvc.perform(get("/events").accept(MediaType.TEXT_HTML))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/access-denied"));
    }

    @Test
    @WithMockUser(authorities = {"EVENT_VIEW", "BOOK_EVENTS"})
    void customer_cannotCreateEvent() throws Exception {
        mockMvc.perform(post("/event/manage/create").with(csrf()).param("name", "Hack"))
                .andExpect(status().isForbidden());
    }

    // --- on-site staff: may see, but not book themselves ---

    @Test
    @WithMockUser(authorities = "EVENT_VIEW")
    void withoutBookEvents_bookingIsForbidden() throws Exception {
        mockMvc.perform(get("/customer/event/{id}/book", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    // --- event provider: EVENT_CREATE ---

    @Test
    @WithMockUser(authorities = {"EVENT_VIEW", "EVENT_CREATE", "EDIT_OWN_EVENTS"})
    void provider_canOpenCreateForm() throws Exception {
        mockMvc.perform(get("/event/manage/create")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"EVENT_VIEW", "EVENT_CREATE", "EDIT_OWN_EVENTS"})
    void provider_cannotOpenRoleManager() throws Exception {
        mockMvc.perform(get("/admin/roles")).andExpect(status().isForbidden());
    }

    // --- finance: no event permissions ---

    @Test
    @WithMockUser(authorities = {"VIEW_ALL_BOOKINGS", "VIEW_PAYMENTS", "MANAGE_PAYMENTS"})
    void finance_cannotSeeCatalog() throws Exception {
        mockMvc.perform(get("/customer/events")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"VIEW_ALL_BOOKINGS", "VIEW_PAYMENTS", "MANAGE_PAYMENTS"})
    void finance_landsOnHomePage() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    // --- REST API ---

    @Test
    @WithMockUser(authorities = "EVENT_VIEW")
    void readOnlyUser_cannotUpdateEventViaApi() throws Exception {
        mockMvc.perform(put("/api/events/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    // --- default deny ---

    @Test
    @WithMockUser(authorities = {"EVENT_VIEW", "BOOK_EVENTS"})
    void unknownUrl_isForbidden() throws Exception {
        mockMvc.perform(get("/some/page/nobody/configured")).andExpect(status().isForbidden());
    }
}