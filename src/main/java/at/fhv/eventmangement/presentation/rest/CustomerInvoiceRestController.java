package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.InvoiceDto;
import at.fhv.eventmangement.application.form.InvoiceForm;
import at.fhv.eventmangement.application.services.InvoiceService;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotEditableException;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customer")
public class CustomerInvoiceRestController {

    private final InvoiceService invoiceService;

    public CustomerInvoiceRestController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/invoice/{id}/info")
    public InvoiceDto getInvoice(@PathVariable UUID id) throws InvoiceNotFoundException {
        return invoiceService.getInvoice(id);
    }

    @PutMapping("/invoice/{id}")
    public InvoiceDto requestChanges(@PathVariable UUID id, @Valid @RequestBody InvoiceForm form)
            throws InvoiceNotFoundException, InvoiceNotEditableException {
        return invoiceService.requestChanges(id, form);
    }

    @PutMapping("/invoice/{id}/confirm")
    public InvoiceDto confirm(@PathVariable UUID id) throws InvoiceNotFoundException, InvoiceNotEditableException {
        return invoiceService.confirm(id);
    }
}
