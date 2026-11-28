package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.*;
import at.fhv.eventmangement.application.services.*;
import at.fhv.eventmangement.domain.exceptions.*;
import at.fhv.eventmangement.domain.model.EventBookingStatus;
import at.fhv.eventmangement.domain.model.EventBookingType;
import at.fhv.eventmangement.domain.model.PaymentMethod;
import at.fhv.eventmangement.presentation.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for the customer Thymeleaf controller (browse + book events).
 */
@WebMvcTest(CustomerEventController.class)
@Import(SecurityConfig.class)
@WithMockUser(authorities = {"EVENT_VIEW", "BOOK_EVENTS", "VIEW_OWN_BOOKINGS"})
class CustomerEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;
    @MockitoBean
    private EventBookingService eventBookingService;
    @MockitoBean
    private CustomerService customerService;
    @MockitoBean
    private InvoiceService invoiceService;
    @MockitoBean
    private EventCategoryService eventCategoryService;

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

    private EventBookingDto booking(UUID id) {
        return new EventBookingDto(id, "BK-ABCDEFGH", null,
                new EventDateDto(eventDateId, LocalDateTime.of(2027, 1, 10, 17, 0), LocalDateTime.of(2027, 1, 10, 20, 0)),
                EventBookingType.GROUP, 2, new BigDecimal("50.00"), PaymentMethod.CARD, null, null,
                EventBookingStatus.CONFIRMED, List.of(), LocalDateTime.of(2026, 11, 25, 10, 0));
    }

    private ResultActions postGroupBooking(int places) throws Exception {
        return mockMvc.perform(post("/customer/event/{id}/book", eventId).with(csrf())
                .param("eventDateId", eventDateId.toString())
                .param("eventBookingType", "GROUP")
                .param("places", String.valueOf(places))
                .param("paymentMethod", "CARD"));
    }

    @Test
    void bookingForm_showsEventAndCustomer() throws Exception {
        when(eventService.getEventById(eventId)).thenReturn(bookableEvent());
        when(customerService.getCurrentCustomer("user")).thenReturn(defaultCustomer());

        mockMvc.perform(get("/customer/event/{id}/book", eventId))
                .andExpect(status().isOk())
                .andExpect(view().name("customer_events"))
                .andExpect(content().string(containsString("Book Summer Hike")))
                .andExpect(content().string(containsString("test-customer")));
    }

    @Test
    void bookingForm_unknownEvent_redirectsToEventListWithError() throws Exception {
        when(customerService.getCurrentCustomer("user")).thenReturn(defaultCustomer());
        when(eventService.getEventById(eventId)).thenThrow(new EventNotFoundException());

        mockMvc.perform(get("/customer/event/{id}/book", eventId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("errorMessage", "event does not exist"));
    }

    @Test
    void bookingForm_accountWithoutCustomer_redirectsToEventListWithError() throws Exception {
        when(customerService.getCurrentCustomer("user")).thenThrow(new CustomerNotFoundException());

        mockMvc.perform(get("/customer/event/{id}/book", eventId))
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("errorMessage", "customer does not exist"));
    }

    @Test
    void book_success_redirectsToInterimInvoice() throws Exception {
        UUID bookingId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        when(eventBookingService.book(any(), any(), eq("user"))).thenReturn(booking(bookingId));
        when(invoiceService.getInvoiceForBooking(bookingId)).thenReturn(new InvoiceDto(invoiceId, bookingId,
                null, null, null, null, List.of(), null, null, null, null, null, true));

        postGroupBooking(2)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/invoice/" + invoiceId))
                .andExpect(flash().attribute("successMessage", "Event successfully booked (BK-ABCDEFGH)"));
    }

    @Test
    void book_success_invoiceMissing_redirectsToEventList() throws Exception {
        UUID bookingId = UUID.randomUUID();
        when(eventBookingService.book(any(), any(), eq("user"))).thenReturn(booking(bookingId));
        when(invoiceService.getInvoiceForBooking(bookingId)).thenThrow(new InvoiceNotFoundException());

        postGroupBooking(2)
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("successMessage", "Event successfully booked (BK-ABCDEFGH)"));
    }

    @Test
    void book_notEnoughPlaces_showsErrorOnPlacesField() throws Exception {
        when(eventBookingService.book(any(), any(), eq("user"))).thenThrow(new NotEnoughPlacesException(3));
        when(eventService.getEventById(eventId)).thenReturn(bookableEvent());

        postGroupBooking(4)
                .andExpect(status().isOk())
                .andExpect(view().name("customer_events"))
                .andExpect(model().attributeHasFieldErrorCode("booking", "places", "notEnoughPlaces"))
                .andExpect(content().string(containsString("only 3 places left")));
    }

    @Test
    void book_eventNotBookable_showsErrorOnDateField() throws Exception {
        when(eventBookingService.book(any(), any(), eq("user"))).thenThrow(new EventNotBookableException());
        when(eventService.getEventById(eventId)).thenReturn(bookableEvent());

        postGroupBooking(2)
                .andExpect(view().name("customer_events"))
                .andExpect(model().attributeHasFieldErrorCode("booking", "eventDateId", "notBookable"))
                .andExpect(content().string(containsString("event is not open for booking")));
    }

    @Test
    void book_accountWithoutCustomer_showsGlobalError() throws Exception {
        when(eventBookingService.book(any(), any(), eq("user"))).thenThrow(new CustomerNotFoundException());
        when(eventService.getEventById(eventId)).thenReturn(bookableEvent());

        postGroupBooking(2)
                .andExpect(view().name("customer_events"))
                .andExpect(model().attributeHasErrors("booking"))
                .andExpect(content().string(containsString("customer does not exist")));
    }

    @Test
    void book_invalidForm_doesNotCallService() throws Exception {
        when(eventService.getEventById(eventId)).thenReturn(bookableEvent());

        mockMvc.perform(post("/customer/event/{id}/book", eventId).with(csrf()))
                .andExpect(view().name("customer_events"))
                .andExpect(model().attributeHasFieldErrors("booking", "eventDateId", "eventBookingType", "paymentMethod"));

        verify(eventBookingService, never()).book(any(), any(), any());
    }

    @Test
    void book_eventDisappearedWhileShowingErrors_redirectsWithError() throws Exception {
        when(eventBookingService.book(any(), any(), eq("user"))).thenThrow(new NotEnoughPlacesException(0));
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

    @Test
    void bookingConfirmation_ownBooking_isShown() throws Exception {
        when(eventBookingService.getBookingByNumber("BK-ABCDEFGH", "user"))
                .thenReturn(Optional.of(booking(UUID.randomUUID())));

        mockMvc.perform(get("/customer/booking/{number}", "BK-ABCDEFGH"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking_confirmation"))
                .andExpect(content().string(containsString("BK-ABCDEFGH")));
    }

    @Test
    void bookingConfirmation_foreignOrUnknownBooking_redirectsWithError() throws Exception {
        when(eventBookingService.getBookingByNumber("BK-OTHER123", "user")).thenReturn(Optional.empty());

        mockMvc.perform(get("/customer/booking/{number}", "BK-OTHER123"))
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("errorMessage", "Booking not found: BK-OTHER123"));
    }

    @Test
    void myBookings_showsBookingsOfLoggedInCustomer() throws Exception {
        when(customerService.getCurrentCustomer("user")).thenReturn(defaultCustomer());
        when(eventBookingService.getBookingsForCurrentCustomer("user")).thenReturn(List.of(booking(UUID.randomUUID())));

        mockMvc.perform(get("/customer/bookings"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer_bookings"))
                .andExpect(content().string(containsString("BK-ABCDEFGH")));
    }

    @Test
    @WithMockUser(authorities = "EVENT_VIEW")
    void myBookings_withoutViewOwnBookings_isForbidden() throws Exception {
        mockMvc.perform(get("/customer/bookings")).andExpect(status().isForbidden());
    }
}