package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.ContactInfoDto;
import at.fhv.eventmangement.application.dto.InvoiceDto;
import at.fhv.eventmangement.application.form.InvoiceForm;
import at.fhv.eventmangement.application.services.InvoiceService;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotEditableException;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotFoundException;
import at.fhv.eventmangement.domain.model.InvoiceStatus;
import at.fhv.eventmangement.domain.model.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
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
              "paymentMethod": "CARD",
              "items": [
                {"description": "Summer Hike", "quantity": 2, "unitPrice": 50.00, "vatRate": 20}
              ]
            }
            """;

    @Test
    void getInvoice_returnsJson() throws Exception {
        when(invoiceService.getInvoice(id)).thenReturn(invoice(InvoiceStatus.INTERM));

        mockMvc.perform(get("/customer/invoice/{id}/info", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceStatus").value("INTERM"))
                .andExpect(jsonPath("$.editable").value(true));
    }

    @Test
    void getInvoice_unknown_returns404() throws Exception {
        when(invoiceService.getInvoice(id)).thenThrow(new InvoiceNotFoundException());

        mockMvc.perform(get("/customer/invoice/{id}/info", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("invoice does not exist"));
    }

    @Test
    void requestChanges_valid_passesItemsToService() throws Exception {
        when(invoiceService.requestChanges(eq(id), any())).thenReturn(invoice(InvoiceStatus.INTERM));

        mockMvc.perform(put("/customer/invoice/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CHANGES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactInfo.name").value("Firma GmbH"));

        verify(invoiceService).requestChanges(eq(id), argThat((InvoiceForm f) ->
                f.getItems().size() == 1 && f.getItems().get(0).getQuantity() == 2));
    }

    @Test
    void requestChanges_invalid_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(put("/customer/invoice/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "address": "A", "email": "x", "paymentMethod": "CARD",
                                 "items": [{"description": "", "quantity": 0, "unitPrice": -1, "vatRate": 120}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors['items[0].quantity']").exists())
                .andExpect(jsonPath("$.errors['items[0].vatRate']").exists());

        verify(invoiceService, never()).requestChanges(any(), any());
    }

    @Test
    void requestChanges_emptyItemList_returns400() throws Exception {
        mockMvc.perform(put("/customer/invoice/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "A", "address": "B", "email": "a@b.at", "paymentMethod": "CARD", "items": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.items").value("an invoice needs at least one item"));
    }

    @Test
    void requestChanges_finalInvoice_returns409() throws Exception {
        when(invoiceService.requestChanges(eq(id), any())).thenThrow(new InvoiceNotEditableException());

        mockMvc.perform(put("/customer/invoice/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CHANGES))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("invoice is already final and cannot be changed"));
    }

    @Test
    void confirm_returnsFinalInvoice() throws Exception {
        when(invoiceService.confirm(id)).thenReturn(invoice(InvoiceStatus.FINAL));

        mockMvc.perform(put("/customer/invoice/{id}/confirm", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceStatus").value("FINAL"))
                .andExpect(jsonPath("$.editable").value(false));
    }
}
