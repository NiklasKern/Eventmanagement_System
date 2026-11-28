package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.EventBookingDto;
import at.fhv.eventmangement.application.dto.EventCategoryDto;
import at.fhv.eventmangement.application.dto.EventDateDto;
import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.services.EventBookingService;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.model.EventBookingStatus;
import at.fhv.eventmangement.domain.model.EventBookingType;
import at.fhv.eventmangement.domain.model.PaymentMethod;
import at.fhv.eventmangement.presentation.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
@WithMockUser(authorities = {"EVENT_VIEW", "BOOK_EVENTS", "VIEW_OWN_BOOKINGS"})
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;
    @MockitoBean
    private EventBookingService eventBookingService;

    private EventDto event(String name) {
        return new EventDto(UUID.randomUUID(), name, "A nice walk", "Dornbirn", new BigDecimal("25.50"), 1, 10,
                "ACTIVE", new EventCategoryDto(UUID.randomUUID(), "Hiking"), null,
                List.of(new EventDateDto(UUID.randomUUID(), LocalDateTime.of(2027, 6, 1, 9, 0),
                        LocalDateTime.of(2027, 6, 1, 17, 0))));
    }

    private EventBookingDto booking() {
        return new EventBookingDto(UUID.randomUUID(), "BK-ABCDEFGH", null,
                new EventDateDto(UUID.randomUUID(), LocalDateTime.of(2027, 5, 1, 9, 0), LocalDateTime.of(2027, 5, 1, 17, 0)),
                EventBookingType.GROUP, 2, new BigDecimal("51.00"), PaymentMethod.CARD, null, null,
                EventBookingStatus.CONFIRMED, List.of(), null);
    }

    private void nothingToShow() throws Exception {
        when(eventService.getActiveEvents()).thenReturn(List.of());
        when(eventBookingService.getNextBookingForCurrentCustomer("user")).thenReturn(Optional.empty());
    }

    // main page (customer)

    @Test
    void mainPage_isReachableUnderRootAndHome() throws Exception {
        nothingToShow();

        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("homepage"));
        mockMvc.perform(get("/home")).andExpect(status().isOk()).andExpect(view().name("homepage"));
    }

    @Test
    void mainPage_showsAtMostThreeEventsWithLinkToBooking() throws Exception {
        EventDto first = event("Summer Hike");
        when(eventService.getActiveEvents()).thenReturn(List.of(first, event("Lake Tour"), event("Rafting"), event("Hidden")));
        when(eventBookingService.getNextBookingForCurrentCustomer("user")).thenReturn(Optional.empty());

        mockMvc.perform(get("/"))
                .andExpect(model().attribute("events", hasSize(3)))
                .andExpect(content().string(containsString("Summer Hike")))
                .andExpect(content().string(containsString("25,50 € per person")))
                .andExpect(content().string(containsString("href=\"/customer/event/" + first.id() + "/book\"")))
                .andExpect(content().string(not(containsString("Hidden"))));
    }

    @Test
    void mainPage_showsNextBookingOfLoggedInCustomer() throws Exception {
        when(eventService.getActiveEvents()).thenReturn(List.of());
        when(eventBookingService.getNextBookingForCurrentCustomer("user")).thenReturn(Optional.of(booking()));

        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("BK-ABCDEFGH")))
                .andExpect(content().string(containsString("01.05.2027, 09:00")))
                .andExpect(content().string(containsString("href=\"/customer/booking/BK-ABCDEFGH\"")));
    }

    @Test
    void mainPage_accountWithoutCustomer_showsEmptyStates() throws Exception {
        when(eventService.getActiveEvents()).thenReturn(List.of());
        when(eventBookingService.getNextBookingForCurrentCustomer("user")).thenThrow(new CustomerNotFoundException());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("You have no upcoming bookings.")))
                .andExpect(content().string(containsString("There are no events open for booking right now.")));
    }

    @Test
    void mainPage_searchLeadsToEventSearch() throws Exception {
        nothingToShow();

        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("action=\"/customer/events/search\"")))
                .andExpect(content().string(containsString("name=\"location\"")))
                .andExpect(content().string(containsString("name=\"dateFrom\"")));
    }

    // main page: only what the role allows

    @Test
    @WithMockUser(authorities = "EVENT_VIEW")
    void mainPage_withoutBookEvents_hasNoBookButtonAndNoBookings() throws Exception {
        when(eventService.getActiveEvents()).thenReturn(List.of(event("Summer Hike")));

        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("Summer Hike")))
                .andExpect(content().string(not(containsString("/book\""))))
                .andExpect(content().string(not(containsString("Your next adventure"))));
        verify(eventBookingService, never()).getNextBookingForCurrentCustomer(any());
    }

    // main page: not logged in

    @Test
    @WithAnonymousUser
    void mainPage_notLoggedIn_showsEventsAndLoginToBook() throws Exception {
        EventDto event = event("Summer Hike");
        when(eventService.getActiveEvents()).thenReturn(List.of(event));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Summer Hike")))
                .andExpect(content().string(containsString("action=\"/customer/events/search\"")))
                // the link leads to the login and afterwards to the booking dialog
                .andExpect(content().string(containsString("href=\"/customer/event/" + event.id() + "/book\"")))
                .andExpect(content().string(containsString("Log in to book")))
                .andExpect(content().string(not(containsString("Your next adventure"))));
        verify(eventBookingService, never()).getNextBookingForCurrentCustomer(any());
    }

    @Test
    @WithAnonymousUser
    void navigation_notLoggedIn_showsPublicLinksAndLogin() throws Exception {
        when(eventService.getActiveEvents()).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString(">Events</a>")))
                .andExpect(content().string(containsString("href=\"/login\"")))
                .andExpect(content().string(not(containsString(">My Bookings</a>"))))
                .andExpect(content().string(not(containsString(">Manage Events</a>"))))
                .andExpect(content().string(not(containsString("Signed in as"))));
    }

    // navigation: only links the user may open

    @Test
    void navigation_customer_seesEventsAndOwnBookingsOnly() throws Exception {
        nothingToShow();

        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("class=\"SiteNav\"")))
                .andExpect(content().string(containsString(">Events</a>")))
                .andExpect(content().string(containsString(">My Bookings</a>")))
                .andExpect(content().string(not(containsString(">Manage Events</a>"))))
                .andExpect(content().string(not(containsString(">Roles</a>"))))
                .andExpect(content().string(not(containsString(">Users</a>"))));
    }

    @Test
    @WithMockUser(authorities = {"EVENT_VIEW", "EVENT_CREATE", "EDIT_OWN_EVENTS"})
    void navigation_provider_seesManageEventsButNoBookings() throws Exception {
        when(eventService.getActiveEvents()).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString(">Manage Events</a>")))
                .andExpect(content().string(not(containsString(">My Bookings</a>"))))
                .andExpect(content().string(not(containsString(">Roles</a>"))));
    }

    @Test
    @WithMockUser(authorities = {"MANAGE_ROLES", "MANAGE_USERS"})
    void navigation_admin_seesRoleAndUserManagement() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("href=\"/admin/roles\"")))
                .andExpect(content().string(containsString("href=\"/admin/users\"")));
    }

    @Test
    void navigation_showsLoggedInUserAndLogout() throws Exception {
        nothingToShow();

        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("Signed in as")))
                .andExpect(content().string(containsString("action=\"/logout\"")));
    }
}