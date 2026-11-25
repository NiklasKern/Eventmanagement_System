package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.*;
import at.fhv.eventmangement.application.form.InvoiceForm;
import at.fhv.eventmangement.application.mappers.InvoiceMapper;
import at.fhv.eventmangement.application.services.EventBookingService;
import at.fhv.eventmangement.application.services.EventCategoryService;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.application.services.InvoiceService;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotEditableException;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotFoundException;
import at.fhv.eventmangement.domain.model.EventBookingType;
import at.fhv.eventmangement.domain.model.InvoiceStatus;
import at.fhv.eventmangement.domain.model.PaymentMethod;
import at.fhv.eventmangement.presentation.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerInvoiceController.class)
@Import(SecurityConfig.class)
@WithMockUser(authorities = {"EVENT_VIEW", "BOOK_EVENTS"})
class CustomerInvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvoiceService invoiceService;
    @MockitoBean
    private EventService eventService;
    @MockitoBean
    private EventBookingService eventBookingService;
    @MockitoBean
    private InvoiceMapper invoiceMapper;
    @MockitoBean
    private EventCategoryService eventCategoryService;

    private final UUID invoiceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(eventBookingService.getPaymentMethods()).thenReturn(List.of("CARD"));
        when(eventService.getActiveEvents()).thenReturn(List.of());
    }

    private InvoiceReviewDto review(boolean editable) {
        EventDto event = new EventDto(UUID.randomUUID(), "Summer Hike", "A nice walk", "Dornbirn",
                new BigDecimal("150.00"), 1, 10, "ACTIVE", null, null, List.of());
        EventBookingDto booking = new EventBookingDto(UUID.randomUUID(), "BK-ABCDEFGH", null,
                new EventDateDto(UUID.randomUUID(), LocalDateTime.of(2027, 5, 1, 9, 0), LocalDateTime.of(2027, 5, 1, 17, 0)),
                EventBookingType.GROUP, 2, null, PaymentMethod.CARD, null, null, null, List.of(), null);
        InvoiceDto invoice = new InvoiceDto(invoiceId, booking.id(), LocalDateTime.of(2026, 11, 23, 10, 0),
                new ContactInfoDto("Anna", "Street 1", "anna@example.com"),
                editable ? InvoiceStatus.INTERM : InvoiceStatus.FINAL, PaymentMethod.CARD,
                List.of(new InvoiceItemDto("Summer Hike", 2, new BigDecimal("150.00"), 20,
                        new BigDecimal("300.00"), new BigDecimal("60.00"), new BigDecimal("360.00"))),
                new BigDecimal("300.00"), new BigDecimal("60.00"), new BigDecimal("360.00"),
                new BigDecimal("108.00"), new BigDecimal("252.00"), editable);
        return new InvoiceReviewDto(invoice, booking, event);
    }

    private InvoiceForm prefilled() {
        return InvoiceForm.builder().name("Anna").address("Street 1").email("anna@example.com")
                .paymentMethod(PaymentMethod.CARD).build();
    }

    @Test
    void showInvoice_interim_showsEditableFormAndTotals() throws Exception {
        InvoiceReviewDto review = review(true);
        when(invoiceService.getInvoiceReview(invoiceId)).thenReturn(review);
        when(invoiceMapper.toForm(review.invoice())).thenReturn(prefilled());

        mockMvc.perform(get("/customer/invoice/{id}", invoiceId))
                .andExpect(status().isOk())
                .andExpect(view().name("customer_events"))
                .andExpect(content().string(containsString("Review your invoice")))
                .andExpect(content().string(containsString("BK-ABCDEFGH")))
                .andExpect(content().string(containsString("360,00 €")))
                .andExpect(content().string(containsString("Confirm invoice")))
                .andExpect(content().string(not(containsString("Save changes"))))
                .andExpect(content().string(containsString("value=\"anna@example.com\"")));
    }

    @Test
    void showInvoice_final_isReadOnly() throws Exception {
        InvoiceReviewDto review = review(false);
        when(invoiceService.getInvoiceReview(invoiceId)).thenReturn(review);
        when(invoiceMapper.toForm(review.invoice())).thenReturn(prefilled());

        mockMvc.perform(get("/customer/invoice/{id}", invoiceId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Final invoice")))
                .andExpect(content().string(not(containsString("Confirm invoice"))));
    }

    @Test
    void showInvoice_unknown_redirectsWithError() throws Exception {
        when(invoiceService.getInvoiceReview(invoiceId)).thenThrow(new InvoiceNotFoundException());

        mockMvc.perform(get("/customer/invoice/{id}", invoiceId))
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("errorMessage", "invoice does not exist"));
    }

    @Test
    void confirmInvoice_valid_savesChangesAndConfirms() throws Exception {
        mockMvc.perform(post("/customer/invoice/{id}/confirm", invoiceId).with(csrf())
                        .param("name", "Firma GmbH")
                        .param("address", "Hauptplatz 1")
                        .param("email", "office@firma.at")
                        .param("paymentMethod", "CARD")
                        .param("items[0].description", "should be ignored"))
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("successMessage", "Invoice confirmed, the final invoice has been issued"));

        verify(invoiceService).confirm(eq(invoiceId), argThat((InvoiceForm f) ->
                f.getName().equals("Firma GmbH") && f.getItems() == null));
    }

    @Test
    void confirmInvoice_invalid_showsErrorsAndDoesNotConfirm() throws Exception {
        when(invoiceService.getInvoiceReview(invoiceId)).thenReturn(review(true));

        mockMvc.perform(post("/customer/invoice/{id}/confirm", invoiceId).with(csrf())
                        .param("name", "")
                        .param("address", "Hauptplatz 1")
                        .param("email", "no-mail")
                        .param("paymentMethod", "CARD"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer_events"))
                .andExpect(model().attributeHasFieldErrors("invoiceForm", "name", "email"))
                .andExpect(content().string(containsString("please enter a valid email address")));

        verify(invoiceService, never()).confirm(any(), any());
    }

    @Test
    void confirmInvoice_alreadyFinal_redirectsWithError() throws Exception {
        when(invoiceService.confirm(eq(invoiceId), any())).thenThrow(new InvoiceNotEditableException());

        mockMvc.perform(post("/customer/invoice/{id}/confirm", invoiceId).with(csrf())
                        .param("name", "Anna")
                        .param("address", "Street 1")
                        .param("email", "anna@example.com")
                        .param("paymentMethod", "CARD"))
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("errorMessage", "invoice is already final and cannot be changed"));
    }

    @Test
    void confirmInvoice_unknown_redirectsWithError() throws Exception {
        when(invoiceService.getInvoiceReview(invoiceId)).thenThrow(new InvoiceNotFoundException());

        mockMvc.perform(post("/customer/invoice/{id}/confirm", invoiceId).with(csrf())
                        .param("name", ""))
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("errorMessage", "invoice does not exist"));
    }
}