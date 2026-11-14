package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.EventCategoryDto;
import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.dto.EventUpdateResultDto;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.EventCategoryNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventDateHasBookingsException;
import at.fhv.eventmangement.domain.exceptions.EventDateNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventNotBookableException;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventOrganizerNotFoundException;
import at.fhv.eventmangement.domain.exceptions.MaxParticipantsBelowBookedPlacesException;
import at.fhv.eventmangement.domain.exceptions.NotEnoughPlacesException;
import at.fhv.eventmangement.application.form.EventBookingForm;
import at.fhv.eventmangement.application.form.EventDateForm;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.application.services.CustomerService;
import at.fhv.eventmangement.application.services.EventBookingService;
import at.fhv.eventmangement.application.services.EventCategoryService;
import at.fhv.eventmangement.application.services.EventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
public class EventController {

    private static final String ADD_EVENT = "add_event";
    private static final String MY_EVENTS = "my_events";
    private static final String CUSTOMER_EVENTS = "customer_events";
    private static final String MODIFY_EVENT = "modify_event";
    private static final String REDIRECT_CUSTOMER_EVENTS = "redirect:/customer/events";
    private static final String REDIRECT_EVENTS = "redirect:/events";

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

    // Dropdown values for the booking dialog in customer_events
    @ModelAttribute("bookingTypes")
    public List<String> bookingTypes() {
        return eventBookingService.getBookingTypes();
    }

    @ModelAttribute("paymentMethods")
    public List<String> paymentMethods() {
        return eventBookingService.getPaymentMethods();
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
        if (bindingResult.hasErrors()) {
            //Just logging
            log.info(bindingResult.getFieldErrors().toString());
            return ADD_EVENT;
        }
        // Check if form is valid (unique name, existing category, ...)
        // Each exception is mapped to the form field it belongs to
        try {
            eventService.addEvent(form);
        } catch (EventAlreadyExistsException ex) {
            bindingResult.rejectValue("name", "duplicate", ex.getMessage());
            return ADD_EVENT;
        } catch (EventCategoryNotFoundException ex) {
            bindingResult.rejectValue("eventCategoryId", "unknown", ex.getMessage());
            return ADD_EVENT;
        } catch (EventOrganizerNotFoundException ex) {
            log.error("Default organizer missing", ex);
            bindingResult.reject("organizer", ex.getMessage());
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
    public String showEvents(Model model) {
        model.addAttribute("events", eventService.getActiveEvents());
        return CUSTOMER_EVENTS;
    }

    // Shows the event list with the booking dialog for the event opened
    @GetMapping("/customer/event/{id}/book")
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

    // Show form to modify event, prefilled with existing data
    @GetMapping("/event/manage/{id}/modify")
    public String modifyEvent(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        EventDto dto;
        try {
            dto = eventService.getEventById(id);
        } catch (EventNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return REDIRECT_EVENTS;
        }
        EventForm form = EventForm.builder()
                .name(dto.name())
                .description(dto.description())
                .location(dto.location())
                .price(dto.price())
                .minParticipants(dto.minParticipants())
                .maxParticipants(dto.maxParticipants())
                .eventCategoryId(dto.eventCategory().id())
                .eventDates(new ArrayList<>(dto.eventDates().stream()
                        .map(d -> EventDateForm.builder().id(d.id()).startTime(d.startTime()).endTime(d.endTime()).build())
                        .toList()))
                .build();
        model.addAttribute("event", form);
        model.addAttribute("eventId", id);
        return MODIFY_EVENT;
    }

    // Saves modified event or show an error message
    @PostMapping("/event/manage/{id}/modify")
    public String modifyEvent(@PathVariable UUID id,
                              @Valid @ModelAttribute("event") EventForm form,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        model.addAttribute("eventId", id);
        if (bindingResult.hasErrors()) {
            return MODIFY_EVENT;
        }

        EventUpdateResultDto result;
        try {
            result = eventService.updateEvent(id, form);
        } catch (EventNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return REDIRECT_EVENTS;
        } catch (EventAlreadyExistsException ex) {
            bindingResult.rejectValue("name", "duplicate", ex.getMessage());
            return MODIFY_EVENT;
        } catch (EventCategoryNotFoundException ex) {
            bindingResult.rejectValue("eventCategoryId", "unknown", ex.getMessage());
            return MODIFY_EVENT;
        } catch (MaxParticipantsBelowBookedPlacesException ex) {
            bindingResult.rejectValue("maxParticipants", "belowBooked", ex.getMessage());
            return MODIFY_EVENT;
        } catch (EventDateHasBookingsException ex) {
            bindingResult.rejectValue("eventDates", "hasBookings", ex.getMessage());
            return MODIFY_EVENT;
        }

        String message = "Successfully saved";
        if (!result.notifiedCustomers().isEmpty()) {
            message += ". " + result.notifiedCustomers().size()
                    + " customers who booked this event have been notified: "
                    + String.join(", ", result.notifiedCustomers());
        }

        redirectAttributes.addFlashAttribute("successMessage", message);
        return REDIRECT_EVENTS;
    }

    @PostMapping(path = "/event/manage/{id}/modify", params = "addDay")
    public String addDayOnModify(@PathVariable UUID id, @ModelAttribute("event") EventForm form, Model model) {
        form.getEventDates().add(new EventDateForm());
        model.addAttribute("eventId", id);
        return MODIFY_EVENT;
    }

    @PostMapping(path = "/event/manage/{id}/modify", params = "removeDay")
    public String removeDayOnModify(@PathVariable UUID id, @ModelAttribute("event") EventForm form,
                                    @RequestParam("removeDay") int index, Model model) {
        if (form.getEventDates().size() > 1 && index >= 0 && index < form.getEventDates().size()) {
            form.getEventDates().remove(index);
        }
        model.addAttribute("eventId", id);
        return MODIFY_EVENT;
    }
}
