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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
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

    // main page

    @Test
    void mainPage_isReachableUnderRootAndHome() throws Exception {
        when(eventService.getActiveEvents()).thenReturn(List.of());
        when(eventBookingService.getNextBookingForCurrentCustomer()).thenReturn(Optional.empty());

        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("homepage"));
        mockMvc.perform(get("/home")).andExpect(status().isOk()).andExpect(view().name("homepage"));
    }

    @Test
    void mainPage_showsAtMostThreeEventsWithLinkToBooking() throws Exception {
        EventDto first = event("Summer Hike");
        when(eventService.getActiveEvents()).thenReturn(List.of(first, event("Lake Tour"), event("Rafting"), event("Hidden")));
        when(eventBookingService.getNextBookingForCurrentCustomer()).thenReturn(Optional.empty());

        mockMvc.perform(get("/"))
                .andExpect(model().attribute("events", hasSize(3)))
                .andExpect(content().string(containsString("Summer Hike")))
                .andExpect(content().string(containsString("25,50 € per person")))
                .andExpect(content().string(containsString("href=\"/customer/event/" + first.id() + "/book\"")))
                .andExpect(content().string(not(containsString("Hidden"))));
    }

    @Test
    void mainPage_showsNextBookingWithLinkToDetails() throws Exception {
        when(eventService.getActiveEvents()).thenReturn(List.of());
        when(eventBookingService.getNextBookingForCurrentCustomer()).thenReturn(Optional.of(booking()));

        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("BK-ABCDEFGH")))
                .andExpect(content().string(containsString("01.05.2027, 09:00")))
                .andExpect(content().string(containsString("href=\"/customer/booking/BK-ABCDEFGH\"")));
    }

    @Test
    void mainPage_withoutBookingsOrEvents_showsEmptyStates() throws Exception {
        when(eventService.getActiveEvents()).thenReturn(List.of());
        when(eventBookingService.getNextBookingForCurrentCustomer()).thenThrow(new CustomerNotFoundException());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("You have no upcoming bookings.")))
                .andExpect(content().string(containsString("There are no events open for booking right now.")));
    }

    @Test
    void mainPage_searchLeadsToEventSearch() throws Exception {
        when(eventService.getActiveEvents()).thenReturn(List.of());
        when(eventBookingService.getNextBookingForCurrentCustomer()).thenReturn(Optional.empty());

        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("action=\"/customer/events/search\"")))
                .andExpect(content().string(containsString("name=\"location\"")))
                .andExpect(content().string(containsString("name=\"dateFrom\"")));
    }

    // navigation bar

    @Test
    void navigation_linksToAllMainPages() throws Exception {
        when(eventService.getActiveEvents()).thenReturn(List.of());
        when(eventBookingService.getNextBookingForCurrentCustomer()).thenReturn(Optional.empty());

        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("class=\"SiteNav\"")))
                .andExpect(content().string(containsString("href=\"/customer/events\"")))
                .andExpect(content().string(containsString("href=\"/customer/bookings\"")))
                .andExpect(content().string(containsString("href=\"/events\"")))
                .andExpect(content().string(not(containsString("/contact"))));
    }
}
