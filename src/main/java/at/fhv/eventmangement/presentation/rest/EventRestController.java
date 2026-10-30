package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.services.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import at.fhv.eventmangement.application.exceptions.InvalidInputException;

import java.util.List;

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

    // 400 - the service rejected input (deplicate name, min > max, etc.)
    @ExceptionHandler(InvalidInputException.class)
    public ProblemDetail handleInvalidInput(InvalidInputException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setProperty("errors", ex.getFieldErrors());
        return pd;
    }

}
