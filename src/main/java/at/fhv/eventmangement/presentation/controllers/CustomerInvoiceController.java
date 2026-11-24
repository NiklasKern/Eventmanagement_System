package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.InvoiceReviewDto;
import at.fhv.eventmangement.application.form.InvoiceForm;
import at.fhv.eventmangement.application.services.EventBookingService;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.application.services.InvoiceService;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotEditableException;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotFoundException;
import at.fhv.eventmangement.application.mappers.InvoiceMapper;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/customer")
public class CustomerInvoiceController {

    private static final String CUSTOMER_EVENTS = "customer_events";
    private static final String REDIRECT_CUSTOMER_EVENTS = "redirect:/customer/events";

    private final InvoiceService invoiceService;
    private final EventService eventService;
    private final EventBookingService eventBookingService;
    private final InvoiceMapper invoiceMapper;

    public CustomerInvoiceController(InvoiceService invoiceService, EventService eventService,
                                     EventBookingService eventBookingService, InvoiceMapper invoiceMapper) {
        this.invoiceService = invoiceService;
        this.eventService = eventService;
        this.eventBookingService = eventBookingService;
        this.invoiceMapper = invoiceMapper;
    }

    @InitBinder("invoiceForm")
    public void restrictInvoiceFields(WebDataBinder binder) {
        binder.setDisallowedFields("items*");
    }

    @ModelAttribute("paymentMethods")
    public List<String> paymentMethods() {
        return eventBookingService.getPaymentMethods();
    }

    @GetMapping("/invoice/{id}")
    public String showInvoice(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            InvoiceReviewDto review = invoiceService.getInvoiceReview(id);
            model.addAttribute("review", review);
            model.addAttribute("invoiceForm", invoiceMapper.toForm(review.invoice()));
        } catch (InvoiceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return REDIRECT_CUSTOMER_EVENTS;
        }
        model.addAttribute("events", eventService.getActiveEvents());
        return CUSTOMER_EVENTS;
    }

    @PostMapping("/invoice/{id}/confirm")
    public String confirmInvoice(@PathVariable UUID id,
                                 @Valid @ModelAttribute("invoiceForm") InvoiceForm form,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (!bindingResult.hasErrors()) {
                invoiceService.confirm(id, form);
                redirectAttributes.addFlashAttribute("successMessage", "Invoice confirmed, the final invoice has been issued");
                return REDIRECT_CUSTOMER_EVENTS;
            }
            model.addAttribute("review", invoiceService.getInvoiceReview(id));
        } catch (InvoiceNotFoundException | InvoiceNotEditableException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return REDIRECT_CUSTOMER_EVENTS;
        }
        model.addAttribute("events", eventService.getActiveEvents());
        return CUSTOMER_EVENTS;
    }
}
