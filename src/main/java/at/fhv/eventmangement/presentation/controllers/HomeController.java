package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.EventBookingDto;
import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.services.EventBookingService;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.model.Permissions;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

// Main page for every logged-in user. What it shows depends on the permissions
@Controller
public class HomeController {

    private static final String HOMEPAGE = "homepage";
    private static final int FEATURED_EVENTS = 3; // the main page shows a preview, "Show all events" leads to the full list

    private final EventService eventService;
    private final EventBookingService eventBookingService;

    public HomeController(EventService eventService, EventBookingService eventBookingService) {
        this.eventService = eventService;
        this.eventBookingService = eventBookingService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, Authentication authentication) {
        model.addAttribute("events", featuredEvents(authentication));
        model.addAttribute("nextBooking", nextBooking(authentication));
        return HOMEPAGE;
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access_denied";
    }

    private List<EventDto> featuredEvents(Authentication authentication) {
        if (!has(authentication, Permissions.EVENT_VIEW)) {
            return List.of();
        }
        return eventService.getActiveEvents().stream().limit(FEATURED_EVENTS).toList();
    }

    private EventBookingDto nextBooking(Authentication authentication) {
        if (!has(authentication, Permissions.VIEW_OWN_BOOKINGS)) {
            return null;
        }
        try {
            return eventBookingService.getNextBookingForCurrentCustomer(authentication.getName()).orElse(null);
        } catch (CustomerNotFoundException ex) {
            // account is not linked to a customer: the page shows "no upcoming bookings"
            return null;
        }
    }

    private static boolean has(Authentication authentication, Permissions permission) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(permission.name()));
    }
}