package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.services.EventBookingService;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HomeController {

    private static final String HOMEPAGE = "homepage";
    private static final int FEATURED_EVENTS = 3; //design choice show three on home page then show rest when show more is pressed

    private final EventService eventService;
    private final EventBookingService eventBookingService;

    public HomeController(EventService eventService, EventBookingService eventBookingService) {
        this.eventService = eventService;
        this.eventBookingService = eventBookingService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("events", eventService.getActiveEvents().stream().limit(FEATURED_EVENTS).toList());
        try {
            model.addAttribute("nextBooking", eventBookingService.getNextBookingForCurrentCustomer().orElse(null));
        } catch (CustomerNotFoundException ex) {
            log.warn("Default customer missing, homepage shows no booking");
            model.addAttribute("nextBooking", null);
        }
        return HOMEPAGE;
    }
}