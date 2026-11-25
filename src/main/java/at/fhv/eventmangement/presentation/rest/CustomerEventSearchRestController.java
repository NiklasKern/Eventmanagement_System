package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.form.EventSearchForm;
import at.fhv.eventmangement.application.services.EventService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerEventSearchRestController {
    private final EventService eventService;

    public CustomerEventSearchRestController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/events/search")
    public List<EventDto> search(@Valid EventSearchForm search) {
        return eventService.searchEvents(search);
    }
}
