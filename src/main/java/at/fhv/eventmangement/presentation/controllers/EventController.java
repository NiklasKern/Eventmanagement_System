package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.EventCategoryDto;
import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.domain.exceptions.ValidationException;
import at.fhv.eventmangement.application.services.EventCategoryService;
import at.fhv.eventmangement.application.services.EventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
public class EventController {

    private static final String ADD_EVENT = "add_event";
    private static final String MY_EVENTS = "my_events";

    private final EventService eventService;
    private final EventCategoryService eventCategoryService;

    //Dependency injection
    public EventController(EventService eventService, EventCategoryService eventCategoryService) {
        this.eventService = eventService;
        this.eventCategoryService = eventCategoryService;
    }

    // Categories for the dropdown in the add_event.html
    @ModelAttribute("categories")
    public List<EventCategoryDto> categories() {
        return eventCategoryService.getAllEventCategories();
    }

    //Show
    @GetMapping(path = "/events")
    public String getAllEventsTemplate(Model model) {
        List<EventDto> events = eventService.getEvents();
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


    @GetMapping(path = "/event/manage/create")
    public String createEvent(Model model) {
        model.addAttribute("event", new EventForm());
        return ADD_EVENT;
    }
}

