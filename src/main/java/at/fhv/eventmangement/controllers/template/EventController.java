package at.fhv.eventmangement.controllers.template;

import at.fhv.eventmangement.domain.dto.EventDto;
import at.fhv.eventmangement.domain.dto.EventForm;
import at.fhv.eventmangement.domain.entities.Event;
import at.fhv.eventmangement.exceptions.ValidationException;
import at.fhv.eventmangement.mappers.EventMapper;
import at.fhv.eventmangement.services.EventService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Slf4j
@Controller
public class EventController {

    private static final String ADD_EVENT = "add_event";
    private static final String MY_EVENTS = "my_events";

    private final EventMapper eventMapper;
    private final EventService eventService;
    private final EventCategoryMapper eventCategoryMapper;
    private final EventCategoryService eventCategoryService;

    //Dependency injection
    public EventController(EventMapper eventMapper, EventService eventService, EventCategoryMapper eventCategoryMapper, EventCategoryService eventCategoryService) {
        this.eventMapper = eventMapper;
        this.eventService = eventService;
        this.eventCategoryMapper = eventCategoryMapper;
        this.eventCategoryService = eventCategoryService;
    }

    // Categories for the dropdown in the add_event.html
    @ModelAttribute("categories")
    public List<String> categories() {
        return eventService.getEventCategories();
    }

    //Show
    @GetMapping(path = "/events")
    public String getAllEventsTemplate(Model model) {
        List<EventDto> events = eventService
                .getEvents().stream().map(eventMapper::toDto).toList();
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
        eventService.addEvent(form);
        return "redirect:/events";
    }


    @GetMapping(path = "/event/manage/create")
    public String createEvent(Model model) {
        model.addAttribute("event", new EventForm());
        return ADD_EVENT;
    }
}

