package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.services.EventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class EventRestController {

    private final EventService eventService;

    public EventRestController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping(path = "/getEvents")
    public List<EventDto> getEvents()
    {
        return eventService.getAllEvents();
    }

    @GetMapping("/customer/event/{id}/info")
    public EventDto getEventInfo(@PathVariable UUID id)
    {
        return eventService.getEventById(id);
    }

}
