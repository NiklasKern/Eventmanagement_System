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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

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
    void finance_seesPublicCatalogButCannotBook() throws Exception {
        mockMvc.perform(get("/customer/events")).andExpect(status().isOk());
        mockMvc.perform(get("/customer/event/{id}/book", UUID.randomUUID())).andExpect(status().isForbidden());
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

    // --- rules added with booking, invoices and cancellation

    @Test
    @WithMockUser(authorities = "EVENT_VIEW")
    void withoutViewOwnBookings_myBookingsIsForbidden() throws Exception {
        mockMvc.perform(get("/customer/bookings")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "EVENT_VIEW")
    void withoutBookEvents_invoiceIsForbidden() throws Exception {
        mockMvc.perform(get("/customer/invoice/{id}", UUID.randomUUID())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"EVENT_VIEW", "BOOK_EVENTS"})
    void customer_cannotCancelEvent() throws Exception {
        mockMvc.perform(post("/event/manage/{id}/cancel", UUID.randomUUID()).with(csrf()))
                .andExpect(status().isForbidden());
    }

    // --- not logged in: public website ---

    @Test
    void visitor_canSeeMainPageEventsAndSearch() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/home")).andExpect(status().isOk());
        mockMvc.perform(get("/customer/events")).andExpect(status().isOk());
        mockMvc.perform(get("/customer/events/search").param("location", "Dornbirn")).andExpect(status().isOk());
    }

    @Test
    void visitor_isSentToLoginWhenBooking() throws Exception {
        mockMvc.perform(get("/customer/event/{id}/book", UUID.randomUUID()).accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void visitor_isSentToLoginForOwnBookings() throws Exception {
        mockMvc.perform(get("/customer/bookings").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void visitor_cannotUseEventApi() throws Exception {
        mockMvc.perform(get("/api/events")).andExpect(status().isUnauthorized());
    }

    // --- default deny ---

    @Test
    @WithMockUser(authorities = {"EVENT_VIEW", "BOOK_EVENTS"})
    void unknownUrl_isForbidden() throws Exception {
        mockMvc.perform(get("/some/page/nobody/configured")).andExpect(status().isForbidden());
    }
}