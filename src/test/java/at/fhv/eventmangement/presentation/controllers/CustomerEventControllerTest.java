package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.*;
import at.fhv.eventmangement.application.services.CustomerService;
import at.fhv.eventmangement.application.services.EventBookingService;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventNotBookableException;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import at.fhv.eventmangement.domain.exceptions.NotEnoughPlacesException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Web layer tests for the customer Thymeleaf controller (browse + book events).
 */
@WebMvcTest(CustomerEventController.class)
class CustomerEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;
    @MockitoBean
    private EventBookingService eventBookingService;
    @MockitoBean
    private CustomerService customerService;

    private final UUID categoryId = UUID.randomUUID();

    private final UUID eventId = UUID.randomUUID();
    private final UUID eventDateId = UUID.randomUUID();

    private EventDto bookableEvent() {
        return new EventDto(eventId, "Summer Hike", "A nice walk", "Dornbirn",
                new BigDecimal("25"), 1, 10, "ACTIVE",
                new EventCategoryDto(categoryId, "Hiking"),
                new EventOrganizerDto(UUID.randomUUID(), new ContactInfoDto("Alpine Club", "Street 1", "a@b.at")),
                List.of(new EventDateDto(eventDateId, LocalDateTime.of(2027, 1, 10, 17, 0),
                                                      LocalDateTime.of(2027, 1, 10, 20, 0))));
    }

    private CustomerDto defaultCustomer() {
        return new CustomerDto(UUID.randomUUID(), new ContactInfoDto("test-customer", "Street 2", "c@test.com"),
                null, null);
    }

    private ResultActions postGroupBooking(int places) throws Exception {
        return mockMvc.perform(post("/customer/event/{id}/book", eventId)
                .param("eventDateId", eventDateId.toString())
                .param("eventBookingType", "GROUP")
                .param("places", String.valueOf(places))
                .param("paymentMethod", "CARD"));
    }

    @Test
    void bookingForm_showsEventAndCustomer() throws Exception {
        when(eventService.getEventById(eventId)).thenReturn(bookableEvent());
        when(customerService.getCurrentCustomer()).thenReturn(defaultCustomer());

        mockMvc.perform(get("/customer/event/{id}/book", eventId))
                .andExpect(status().isOk())
                .andExpect(view().name("customer_events"))
                .andExpect(content().string(containsString("Book Summer Hike")))
                .andExpect(content().string(containsString("test-customer")));
    }

    @Test
    void bookingForm_unknownEvent_redirectsToEventListWithError() throws Exception {
        when(customerService.getCurrentCustomer()).thenReturn(defaultCustomer());
        when(eventService.getEventById(eventId)).thenThrow(new EventNotFoundException());

        mockMvc.perform(get("/customer/event/{id}/book", eventId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("errorMessage", "event does not exist"));
    }

    @Test
    void bookingForm_missingDefaultCustomer_redirectsToEventListWithError() throws Exception {
        when(customerService.getCurrentCustomer()).thenThrow(new CustomerNotFoundException());

        mockMvc.perform(get("/customer/event/{id}/book", eventId))
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("errorMessage", "customer does not exist"));
    }

    @Test
    void book_success_redirectsWithBookingNumber() throws Exception {
        when(eventBookingService.book(any(), any())).thenReturn(new EventBookingDto(
                UUID.randomUUID(), "BK-ABCDEFGH", null, null, null, 2, null, null, null, null, null, List.of()));

        postGroupBooking(2)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("successMessage", "Event successfully booked (BK-ABCDEFGH)"));
    }

    @Test
    void book_notEnoughPlaces_showsErrorOnPlacesField() throws Exception {
        when(eventBookingService.book(any(), any())).thenThrow(new NotEnoughPlacesException(3));
        when(eventService.getEventById(eventId)).thenReturn(bookableEvent());

        postGroupBooking(4)
                .andExpect(status().isOk())
                .andExpect(view().name("customer_events"))
                .andExpect(model().attributeHasFieldErrorCode("booking", "places", "notEnoughPlaces"))
                .andExpect(content().string(containsString("only 3 places left")));
    }

    @Test
    void book_eventNotBookable_showsErrorOnDateField() throws Exception {
        when(eventBookingService.book(any(), any())).thenThrow(new EventNotBookableException());
        when(eventService.getEventById(eventId)).thenReturn(bookableEvent());

        postGroupBooking(2)
                .andExpect(view().name("customer_events"))
                .andExpect(model().attributeHasFieldErrorCode("booking", "eventDateId", "notBookable"))
                .andExpect(content().string(containsString("event is not open for booking")));
    }

    @Test
    void book_missingDefaultCustomer_showsGlobalError() throws Exception {
        when(eventBookingService.book(any(), any())).thenThrow(new CustomerNotFoundException());
        when(eventService.getEventById(eventId)).thenReturn(bookableEvent());

        postGroupBooking(2)
                .andExpect(view().name("customer_events"))
                .andExpect(model().attributeHasErrors("booking"))
                .andExpect(content().string(containsString("customer does not exist")));
    }

    @Test
    void book_invalidForm_doesNotCallService() throws Exception {
        when(eventService.getEventById(eventId)).thenReturn(bookableEvent());

        mockMvc.perform(post("/customer/event/{id}/book", eventId))
                .andExpect(view().name("customer_events"))
                .andExpect(model().attributeHasFieldErrors("booking", "eventDateId", "eventBookingType", "paymentMethod"));

        verify(eventBookingService, never()).book(any(), any());
    }

    @Test
    void book_eventDisappearedWhileShowingErrors_redirectsWithError() throws Exception {
        when(eventBookingService.book(any(), any())).thenThrow(new NotEnoughPlacesException(0));
        when(eventService.getEventById(eventId)).thenThrow(new EventNotFoundException());

        postGroupBooking(2)
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("errorMessage", "event does not exist"));
    }

    @Test
    void customerEvents_showsErrorFlash() throws Exception {
        when(eventService.getActiveEvents()).thenReturn(List.of());

        mockMvc.perform(get("/customer/events").flashAttr("errorMessage", "event does not exist"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FlashError")))
                .andExpect(content().string(containsString("event does not exist")));
    }
}
