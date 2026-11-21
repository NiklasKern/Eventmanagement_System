package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.services.EventBookingService;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import lombok.extern.slf4j.Slf4j;
import at.fhv.eventmangement.domain.model.Permissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

import java.net.Authenticator;

@Slf4j
@Controller
public class HomeController {
    @GetMapping("/")
    public String home(Authentication authentication) {
        if (has(authentication, Permissions.EVENT_CREATE) || has(authentication, Permissions.EDIT_ALL_EVENTS)) {
            return "redirect:/events"; //for event providers, management, admin
        }
        if (has(authentication, Permissions.EVENT_VIEW)) {
            return "redirect:/customer/events"; // for customer, on-site staff
        }
        return "home"; //rest
    }

    private static final String HOMEPAGE = "homepage";
    private static final int FEATURED_EVENTS = 3; //design choice show three on home page then show rest when show more is pressed

    private final EventService eventService;
    private final EventBookingService eventBookingService;

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access_denied";
    }

    private static boolean has(Authentication authentication, Permissions permissions) {
        return authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(permissions.name()));
    }

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