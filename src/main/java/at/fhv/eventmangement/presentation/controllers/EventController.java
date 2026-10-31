package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.EventCategoryDto;
import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.form.EventBookingForm;
import at.fhv.eventmangement.application.form.EventDateForm;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.application.services.CustomerService;
import at.fhv.eventmangement.application.services.EventBookingService;
import at.fhv.eventmangement.domain.exceptions.ValidationException;
import at.fhv.eventmangement.application.services.EventCategoryService;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.domain.model.EventBookingType;
import at.fhv.eventmangement.domain.model.PaymentMethod;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
public class EventController {

    private static final String ADD_EVENT = "add_event";
    private static final String MY_EVENTS = "my_events";
    private static final String BOOK_EVENT = "book_event";
    private static final String CUSTOMER_EVENTS = "customer_events";

    private final EventService eventService;
    private final EventCategoryService eventCategoryService;
    private final EventBookingService eventBookingService;
    private final CustomerService customerService;

    //Dependency injection
    public EventController(EventService eventService, EventCategoryService eventCategoryService, EventBookingService eventBookingService, CustomerService customerService) {
        this.eventService = eventService;
        this.eventCategoryService = eventCategoryService;
        this.eventBookingService = eventBookingService;
        this.customerService = customerService;
    }

    // Categories for the dropdown in the add_event
    @ModelAttribute("categories")
    public List<EventCategoryDto> categories() {
        return eventCategoryService.getAllEventCategories();
    }

    // Dropdown values for the book_event
    @ModelAttribute("bookingTypes")
    public EventBookingType[] bookingTypes() {
        return EventBookingType.values();
    }

    @ModelAttribute("paymentMethods")
    public PaymentMethod[] paymentMethods() {
        return PaymentMethod.values();
    }

    //Show
    @GetMapping(path = "/events")
    public String getAllEventsTemplate(Model model) {
        List<EventDto> events = eventService.getAllEvents();
        model.addAttribute("events", events);
        return MY_EVENTS;
    }

    // Saves new event from Create Event form
    // On error shows form again with invalid fields marked
    // On success redirect to event list
    @PostMapping("/event/manage/create")
    public String createEvent(@Valid @ModelAttribute("event") EventForm form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes
    ) {
        // Check for annotations on EventForm(@NotBlank, @Postive, ...)
        if(bindingResult.hasErrors()) {
            //Just logging
            log.info(bindingResult.getFieldErrors().toString());
            return ADD_EVENT;
        }
        // Check if form is valid ( Mandatory, Date in the future, ...)
        try {
            eventService.addEvent(form);
        } catch (ValidationException ex) {
            ex.getFieldErrors().forEach((field, msg) ->
                    bindingResult.rejectValue(field, "invalid", msg));
            return ADD_EVENT;
        }

        redirectAttributes.addFlashAttribute("successMessage", "Event successfully saved");
        return "redirect:/events";
    }


    // Form starts with one empty date row
    @GetMapping(path = "/event/manage/create")
    public String createEvent(Model model) {
        EventForm form = new EventForm();
        form.getEventDates().add(new EventDateForm());
        model.addAttribute("event", form);
        return ADD_EVENT;
    }


    //Adds event date form to event template
    @PostMapping(path = "/event/manage/create", params = "addDay")
    public String addDay(@ModelAttribute("event") EventForm form) {
        form.getEventDates().add(new EventDateForm());
        return ADD_EVENT;
    }

    @PostMapping(path = "/event/manage/create", params = "removeDay")
    public String removeDay(@ModelAttribute("event") EventForm form,
                            @RequestParam("removeDay") int index) {
        if (form.getEventDates().size() > 1 && index < form.getEventDates().size()) {
            form.getEventDates().remove(index);
        }
        return ADD_EVENT;
    }

    //Return active events
    @GetMapping(path = "/customer/events")
    public String showEvents(Model model)
    {
        model.addAttribute("events", eventService.getActiveEvents());
        return CUSTOMER_EVENTS;
    }

    @GetMapping("/customer/event/{id}/book")
    public String bookingForm(@PathVariable UUID id, Model model) {
        model.addAttribute("customer", customerService.getCurrentCustomer());
        model.addAttribute("event", eventService.getEventById(id));
        model.addAttribute("booking", new EventBookingForm());
        return BOOK_EVENT;
    }

    // Saves booking, on error shows form again with invalid fields marked
    @PostMapping("/customer/event/{id}/book")
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
                return "redirect:/customer/event-list";
            } catch (ValidationException ex) {
                ex.getFieldErrors().forEach((field, msg) ->
                        bindingResult.rejectValue(field, "invalid", msg));
            }
        }
        model.addAttribute("event", eventService.getEventById(id));
        return BOOK_EVENT;
    }
}
