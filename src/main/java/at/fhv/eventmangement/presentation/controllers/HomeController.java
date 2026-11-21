package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.domain.model.Permissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

import java.net.Authenticator;

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

    private static boolean has(Authentication authentication, Permissions permissions) {
        return authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(permissions.name()));
    }

}
