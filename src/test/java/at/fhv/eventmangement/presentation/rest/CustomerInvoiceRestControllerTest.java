package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.ContactInfoDto;
import at.fhv.eventmangement.application.dto.InvoiceDto;
import at.fhv.eventmangement.application.form.InvoiceForm;
import at.fhv.eventmangement.application.services.InvoiceService;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotEditableException;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotFoundException;
import at.fhv.eventmangement.domain.model.InvoiceStatus;
import at.fhv.eventmangement.domain.model.PaymentMethod;
import at.fhv.eventmangement.presentation.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerInvoiceRestController.class)
@Import(SecurityConfig.class)
@WithMockUser(authorities = {"EVENT_VIEW", "BOOK_EVENTS"})
class CustomerInvoiceRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvoiceService invoiceService;

    private final UUID id = UUID.randomUUID();

    private InvoiceDto invoice(InvoiceStatus status) {
        return new InvoiceDto(id, UUID.randomUUID(), LocalDateTime.now(),
                new ContactInfoDto("Firma GmbH", "Hauptplatz 1", "office@firma.at"), status, PaymentMethod.CARD,
                List.of(), new BigDecimal("100.00"), new BigDecimal("20.00"), new BigDecimal("120.00"),
                new BigDecimal("36.00"), new BigDecimal("84.00"), status == InvoiceStatus.INTERM);
    }

    private static final String VALID_CHANGES = """
            {
              "name": "Firma GmbH",
              "address": "Hauptplatz 1",
              "email": "office@firma.at",
              "paymentMethod": "CARD"
            }
            """;

    @Test
    void getInvoice_returnsJson() throws Exception {
        when(invoiceService.getInvoice(id, "user")).thenReturn(invoice(InvoiceStatus.INTERM));

        mockMvc.perform(get("/api/customer/invoice/{id}/info", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceStatus").value("INTERM"))
                .andExpect(jsonPath("$.editable").value(true));
    }

    @Test
    void getInvoice_unknownOrForeign_returns404() throws Exception {
        when(invoiceService.getInvoice(id, "user")).thenThrow(new InvoiceNotFoundException());

        mockMvc.perform(get("/api/customer/invoice/{id}/info", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("invoice does not exist"));
    }

    @Test
    void requestChanges_valid_passesAddressToServiceForLoggedInUser() throws Exception {
        when(invoiceService.requestChanges(eq(id), any(), eq("user"))).thenReturn(invoice(InvoiceStatus.INTERM));

        mockMvc.perform(put("/api/customer/invoice/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CHANGES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactInfo.name").value("Firma GmbH"));

        verify(invoiceService).requestChanges(eq(id), argThat((InvoiceForm f) ->
                f.getName().equals("Firma GmbH") && f.getAddress().equals("Hauptplatz 1")), eq("user"));
    }

    @Test
    void requestChanges_ownPricesInRequest_areIgnored() throws Exception {
        when(invoiceService.requestChanges(eq(id), any(), eq("user"))).thenReturn(invoice(InvoiceStatus.INTERM));

        mockMvc.perform(put("/api/customer/invoice/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Firma GmbH", "address": "Hauptplatz 1", "email": "office@firma.at",
                                 "paymentMethod": "CARD",
                                 "items": [{"description": "Summer Hike", "quantity": 2, "unitPrice": 0.01, "vatRate": 0}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grossTotal").value(120.00));
    }

    @Test
    void requestChanges_invalid_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(put("/api/customer/invoice/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "address": "A", "email": "x", "paymentMethod": "CARD"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists());

        verify(invoiceService, never()).requestChanges(any(), any(), any());
    }

    @Test
    void requestChanges_finalInvoice_returns409() throws Exception {
        when(invoiceService.requestChanges(eq(id), any(), eq("user"))).thenThrow(new InvoiceNotEditableException());

        mockMvc.perform(put("/api/customer/invoice/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CHANGES))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("invoice is already final and cannot be changed"));
    }

    @Test
    void requestChanges_unknownOrForeign_returns404() throws Exception {
        when(invoiceService.requestChanges(eq(id), any(), eq("user"))).thenThrow(new InvoiceNotFoundException());

        mockMvc.perform(put("/api/customer/invoice/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CHANGES))
                .andExpect(status().isNotFound());
    }

    @Test
    void confirm_returnsFinalInvoice() throws Exception {
        when(invoiceService.confirm(id, "user")).thenReturn(invoice(InvoiceStatus.FINAL));

        mockMvc.perform(put("/api/customer/invoice/{id}/confirm", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceStatus").value("FINAL"))
                .andExpect(jsonPath("$.editable").value(false));
    }

    @Test
    @WithMockUser(authorities = "EVENT_VIEW")
    void withoutBookEvents_invoiceApiIsForbidden() throws Exception {
        mockMvc.perform(get("/api/customer/invoice/{id}/info", id)).andExpect(status().isForbidden());
        verify(invoiceService, never()).getInvoice(any(), any());
    }
}