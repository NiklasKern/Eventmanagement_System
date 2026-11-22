package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.EventCategoryDto;
import at.fhv.eventmangement.application.form.EventBookingForm;
import at.fhv.eventmangement.application.form.EventSearchForm;
import at.fhv.eventmangement.application.services.CustomerService;
import at.fhv.eventmangement.application.services.EventBookingService;
import at.fhv.eventmangement.application.services.EventCategoryService;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.domain.exceptions.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
    private final EventCategoryService eventCategoryService;

    //Dependency injection
    public CustomerEventController(EventService eventService, EventBookingService eventBookingService, CustomerService customerService,
                                   EventCategoryService eventCategoryService) {
        this.eventService = eventService;
        this.eventBookingService = eventBookingService;
        this.customerService = customerService;
        this.eventCategoryService = eventCategoryService;
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
    public String bookingForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes,
                              Authentication authentication) {
        try {
            model.addAttribute("customer", customerService.getCurrentCustomer(authentication.getName()));
            model.addAttribute("event", eventService.getEventById(id));
        } catch (EventNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return REDIRECT_CUSTOMER_EVENTS;
        } catch (CustomerNotFoundException ex) {
            // account is not linked to a customer
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
                       RedirectAttributes redirectAttributes,
                       Authentication authentication) {
        if (!bindingResult.hasErrors()) {
            try {
                String bookingNumber = eventBookingService.book(id, form, authentication.getName()).bookingNumber();
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
                // account is not linked to a customer
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

    @ModelAttribute("search")
    public EventSearchForm searchForm() {
        return new EventSearchForm();
    }

    @ModelAttribute("categories")
    public List<EventCategoryDto> categories() {
        return eventCategoryService.getAllEventCategories();
    }

    @ModelAttribute("locations")
    public List<String> locations() {
        return eventService.getEventLocation();
    }

    @GetMapping("/events/search")
    public String search(@Valid @ModelAttribute("search") EventSearchForm search, BindingResult bindingResult, Model model) {
        model.addAttribute("searched", true);
        model.addAttribute("events", bindingResult.hasErrors() ? List.of() : eventService.searchEvents(search));
        return CUSTOMER_EVENTS;
    }
}