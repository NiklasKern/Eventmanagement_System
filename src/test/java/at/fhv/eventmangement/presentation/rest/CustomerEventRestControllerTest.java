package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.ContactInfoDto;
import at.fhv.eventmangement.application.dto.InvoiceDto;
import at.fhv.eventmangement.application.form.EventBookingForm;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.application.services.InvoiceService;
import at.fhv.eventmangement.domain.model.EventBookingType;
import at.fhv.eventmangement.domain.model.InvoiceStatus;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import at.fhv.eventmangement.presentation.security.SecurityConfig;
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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REST API tests for the customer endpoints.
 */
@WebMvcTest(CustomerEventRestController.class)
@Import(SecurityConfig.class)
@WithMockUser(authorities = {"EVENT_VIEW", "BOOK_EVENTS"})
class CustomerEventRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private InvoiceService invoiceService;

    @Test
    void unknownEvent_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(eventService.getEventById(id)).thenThrow(new EventNotFoundException());

        mockMvc.perform(get("/customer/event/{id}/info", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("event does not exist"));
    }

    @Test
    void invoicePreview_bindsQueryParamsAndReturnsJson() throws Exception {
        UUID id = UUID.randomUUID();
        InvoiceDto preview = new InvoiceDto(null, null, LocalDateTime.now(),
                new ContactInfoDto("Anna", "Street 1", "anna@example.com"), InvoiceStatus.INTERM, null, List.of(),
                new BigDecimal("300.00"), new BigDecimal("60.00"), new BigDecimal("360.00"),
                new BigDecimal("108.00"), new BigDecimal("252.00"), true);
        when(invoiceService.previewInvoice(eq(id), argThat((EventBookingForm f) ->
                f.getEventBookingType() == EventBookingType.GROUP && Integer.valueOf(2).equals(f.getPlaces())), eq("user")))
                .thenReturn(preview);

        mockMvc.perform(get("/customer/event/{id}/invoice-preview", id)
                        .param("eventBookingType", "GROUP")
                        .param("places", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceStatus").value("INTERM"))
                .andExpect(jsonPath("$.grossTotal").value(360.00))
                .andExpect(jsonPath("$.contactInfo.name").value("Anna"));
    }
}