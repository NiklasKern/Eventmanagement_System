package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.form.EventBookingForm;
import at.fhv.eventmangement.application.services.CustomerService;
import at.fhv.eventmangement.application.services.EventBookingService;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventDateNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventNotBookableException;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import at.fhv.eventmangement.domain.exceptions.NotEnoughPlacesException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

// Customer side: browse active events and book them
@Slf4j
@Controller
@RequestMapping("/customer")
public class CustomerEventController {

    private static final String CUSTOMER_EVENTS = "customer_events";
    private static final String REDIRECT_CUSTOMER_EVENTS = "redirect:/customer/events";

    private final EventService eventService;
    private final EventBookingService eventBookingService;
    private final CustomerService customerService;

    //Dependency injection
    public CustomerEventController(EventService eventService, EventBookingService eventBookingService, CustomerService customerService) {
        this.eventService = eventService;
        this.eventBookingService = eventBookingService;
        this.customerService = customerService;
    }

    // Dropdown values for the booking dialog in customer_events
    @ModelAttribute("bookingTypes")
    public List<String> bookingTypes() {
        return eventBookingService.getBookingTypes();
    }

    @ModelAttribute("paymentMethods")
    public List<String> paymentMethods() {
        return eventBookingService.getPaymentMethods();
    }

    //Return active events
    @GetMapping("/events")
    public String showEvents(Model model) {
        model.addAttribute("events", eventService.getActiveEvents());
        return CUSTOMER_EVENTS;
    }

    // Shows the event list with the booking dialog for the event opened
    @GetMapping("/event/{id}/book")
    public String bookingForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("customer", customerService.getCurrentCustomer());
            model.addAttribute("event", eventService.getEventById(id));
        } catch (EventNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return REDIRECT_CUSTOMER_EVENTS;
        } catch (CustomerNotFoundException ex) {
            log.error("Default customer missing", ex);
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return REDIRECT_CUSTOMER_EVENTS;
        }
        model.addAttribute("booking", new EventBookingForm());
        model.addAttribute("events", eventService.getActiveEvents());
        return CUSTOMER_EVENTS;
    }

    // Saves booking, on error shows form again with invalid fields marked
    @PostMapping("/event/{id}/book")
    public String book(@PathVariable UUID id,
                       @Valid @ModelAttribute("booking") EventBookingForm form,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                String bookingNumber = eventBookingService.book(id, form).bookingNumber();
                redirectAttributes.addFlashAttribute("successMessage",
                        "Event successfully booked (" + bookingNumber + ")");
                return REDIRECT_CUSTOMER_EVENTS;
            } catch (EventDateNotFoundException ex) {
                bindingResult.rejectValue("eventDateId", "unknown", ex.getMessage());
            } catch (EventNotBookableException ex) {
                bindingResult.rejectValue("eventDateId", "notBookable", ex.getMessage());
            } catch (NotEnoughPlacesException ex) {
                bindingResult.rejectValue("places", "notEnoughPlaces", ex.getMessage());
            } catch (CustomerNotFoundException ex) {
                log.error("Default customer missing", ex);
                bindingResult.reject("customer", ex.getMessage());
            }
        }
        // Show the booking dialog again with the errors
        try {
            model.addAttribute("event", eventService.getEventById(id));
        } catch (EventNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return REDIRECT_CUSTOMER_EVENTS;
        }
        model.addAttribute("events", eventService.getActiveEvents());
        return CUSTOMER_EVENTS;
    }
}
