package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.dto.InvoiceDto;
import at.fhv.eventmangement.application.form.EventBookingForm;
import at.fhv.eventmangement.application.form.EventSearchForm;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.application.services.InvoiceService;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/customer")
public class CustomerEventRestController {

    private final EventService eventService;
    private final InvoiceService invoiceService;

    public CustomerEventRestController(EventService eventService, InvoiceService invoiceService) {
        this.eventService = eventService;
        this.invoiceService = invoiceService;
    }

    @GetMapping("/event/{id}/info")
    public EventDto getEventInfo(@PathVariable UUID id) throws EventNotFoundException {
        return eventService.getEventById(id);
    }

    @GetMapping("/api/customer/events/search")
    public List<EventDto> search(@Valid EventSearchForm search) {
        return eventService.searchEvents(search);
    }

    @GetMapping("/event/{id}/invoice-preview")
    public InvoiceDto getInvoicePreview(@PathVariable UUID id, @ModelAttribute EventBookingForm form,
                                        Authentication authentication) throws EventNotFoundException, CustomerNotFoundException {
        return invoiceService.previewInvoice(id, form, authentication.getName());
    }
}
