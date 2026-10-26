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

@Controller
public class EventController {

    private final EventMapper eventMapper;
    private final EventService eventService;

    //Dependency injection
    public EventController(EventMapper eventMapper, EventService eventService) {
        this.eventMapper = eventMapper;
        this.eventService = eventService;
    }

    //Show
    @GetMapping(path = "/events")
    public String getAllEventsTemplate(Model model) {
        List<EventDto> events = eventService
                .getEvents().stream().map(eventMapper::toDto).toList();
        model.addAttribute("events", events);
        return "Events";
    }

    @PostMapping("/manage/event/create")
    public String createAuthor(@Valid @ModelAttribute("event") EventForm form, BindingResult bindingResult) {

        //Return create event form with error
        if(bindingResult.hasErrors()) {
            return "CreateEvent";
        }
        try {
            eventService.addEvent(form);
        } catch (ValidationException ex) {
            ex.getFieldErrors().forEach((field, msg) ->
                    bindingResult.rejectValue(field, "invalid", msg));
            return "CreateEvent";
        }

        return "redirect:/events";
    }


    @GetMapping(path = "/event/manage/create")
    public String createEvent(Model model) {
        model.addAttribute("event",new EventForm());
        return "CreateEvent";
    }
}

