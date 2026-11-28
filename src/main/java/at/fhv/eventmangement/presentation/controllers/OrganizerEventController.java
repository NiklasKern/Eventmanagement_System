package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.EventCategoryDto;
import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.dto.EventOrganizerDto;
import at.fhv.eventmangement.application.dto.EventUpdateResultDto;
import at.fhv.eventmangement.application.form.EventDateForm;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.application.services.EventCategoryService;
import at.fhv.eventmangement.application.services.EventOrganizerService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
public class OrganizerEventController {

    private static final String MANAGE = "/event/manage";
    private static final String CREATE = MANAGE + "/create";
    private static final String MODIFY = MANAGE + "/{id}/modify";
    private static final String CANCEL = MANAGE + "/{id}/cancel";
    private static final String ADD_EVENT = "add_event";
    private static final String MY_EVENTS = "my_events";
    private static final String MODIFY_EVENT = "modify_event";
    private static final String REDIRECT_EVENTS = "redirect:/events";

    private final EventService eventService;
    private final EventCategoryService eventCategoryService;
    private final EventOrganizerService eventOrganizerService;

    //Dependency injection
    public OrganizerEventController(EventService eventService, EventCategoryService eventCategoryService,
                                    EventOrganizerService eventOrganizerService) {
        this.eventService = eventService;
        this.eventCategoryService = eventCategoryService;
        this.eventOrganizerService = eventOrganizerService;
    }

    // Categories for the dropdown in add_event and modify_event
    @ModelAttribute("categories")
    public List<EventCategoryDto> categories() {
        return eventCategoryService.getAllEventCategories();
    }

    // Organizer dropdown in add_event (only shown with EDIT_ALL_EVENTS)
    @ModelAttribute("organizers")
    public List<EventOrganizerDto> organizers() {
        return eventOrganizerService.getAllOrganizers();
    }

    // Shows the events the logged-in user may manage
    @GetMapping("/events")
    public String getAllEventsTemplate(Model model, Authentication authentication) {
        List<EventDto> events = eventService.getManagedEvents(authentication.getName());
        model.addAttribute("events", events);
        return MY_EVENTS;
    }

    // Form starts with one empty date row
    @GetMapping(CREATE)
    public String createEvent(Model model) {
        EventForm form = new EventForm();
        form.getEventDates().add(new EventDateForm());
        model.addAttribute("event", form);
        return ADD_EVENT;
    }

    // Saves new event from Create Event form
    // On error shows form again with invalid fields marked
    // On success redirect to event list
    @PostMapping(CREATE)
    public String createEvent(@Valid @ModelAttribute("event") EventForm form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Authentication authentication
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
            eventService.addEvent(form, authentication.getName());
        } catch (EventAlreadyExistsException ex) {
            bindingResult.rejectValue("name", "duplicate", ex.getMessage());
            return ADD_EVENT;
        } catch (EventCategoryNotFoundException ex) {
            bindingResult.rejectValue("eventCategoryId", "unknown", ex.getMessage());
            return ADD_EVENT;
        } catch (EventOrganizerNotFoundException ex) {
            // account not linked to an organizer and none chosen in the form
            bindingResult.reject("organizer", ex.getMessage());
            return ADD_EVENT;
        }

        redirectAttributes.addFlashAttribute("successMessage", "Event successfully saved");
        return REDIRECT_EVENTS;
    }

    //Adds event date form to event template
    @PostMapping(path = CREATE, params = "addDay")
    public String addDay(@ModelAttribute("event") EventForm form) {
        form.getEventDates().add(new EventDateForm());
        return ADD_EVENT;
    }

    @PostMapping(path = CREATE, params = "removeDay")
    public String removeDay(@ModelAttribute("event") EventForm form,
                            @RequestParam("removeDay") int index) {
        if (form.getEventDates().size() > 1 && index < form.getEventDates().size()) {
            form.getEventDates().remove(index);
        }
        return ADD_EVENT;
    }

    @GetMapping(MODIFY)
    public String modifyEvent(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes,
                              Authentication authentication) {
        EventDto dto;
        try {
            dto = eventService.getEventForEdit(id, authentication.getName());
        } catch (EventNotFoundException | NotEventOwnerException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return REDIRECT_EVENTS;
        }
        if ("CANCELLED".equals(dto.eventStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cancelled events cannot be modified.");
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
        model.addAttribute("eventStatus", dto.eventStatus());
        return MODIFY_EVENT;
    }

    // Saves modified event or show an error message
    @PostMapping(MODIFY)
    public String modifyEvent(@PathVariable UUID id,
                              @Valid @ModelAttribute("event") EventForm form,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes,
                              Authentication authentication) {
        model.addAttribute("eventId", id);
        if (bindingResult.hasErrors()) {
            return MODIFY_EVENT;
        }

        EventUpdateResultDto result;
        try {
            result = eventService.updateEvent(id, form, authentication.getName());
        } catch (EventNotFoundException | NotEventOwnerException ex) {
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

    @PostMapping(path = MODIFY, params = "addDay")
    public String addDayOnModify(@PathVariable UUID id, @ModelAttribute("event") EventForm form, Model model) {
        form.getEventDates().add(new EventDateForm());
        model.addAttribute("eventId", id);
        return MODIFY_EVENT;
    }

    @PostMapping(path = MODIFY, params = "removeDay")
    public String removeDayOnModify(@PathVariable UUID id, @ModelAttribute("event") EventForm form,
                                    @RequestParam("removeDay") int index, Model model) {
        if (form.getEventDates().size() > 1 && index >= 0 && index < form.getEventDates().size()) {
            form.getEventDates().remove(index);
        }
        model.addAttribute("eventId", id);
        return MODIFY_EVENT;
    }

    @PostMapping(CANCEL)
    public String cancelEvent(@PathVariable UUID id, RedirectAttributes redirectAttributes, Authentication authentication) {
        EventUpdateResultDto result;
        try {
            result = eventService.cancelEvent(id, authentication.getName());
        } catch (EventNotFoundException | EventAlreadyCancelledException | NotEventOwnerException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return REDIRECT_EVENTS;
        }

        String message = "Event successfully cancelled";
        if (!result.notifiedCustomers().isEmpty()) {
            message += ". " + result.notifiedCustomers().size()
                    + " customers who booked this event have been notified: "
                    + String.join(", ", result.notifiedCustomers());
        }

        redirectAttributes.addFlashAttribute("successMessage", message);
        return REDIRECT_EVENTS;
    }
}