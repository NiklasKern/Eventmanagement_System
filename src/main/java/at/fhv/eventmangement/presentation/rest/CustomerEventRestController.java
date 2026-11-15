package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/customer")
public class CustomerEventRestController {

    private final EventService eventService;

    public CustomerEventRestController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/event/{id}/info")
    public EventDto getEventInfo(@PathVariable UUID id) throws EventNotFoundException {
        return eventService.getEventById(id);
    }
}
