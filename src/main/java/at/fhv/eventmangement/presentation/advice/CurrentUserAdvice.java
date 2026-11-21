package at.fhv.eventmangement.presentation.advice;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// Adds logged-in user into every page
@ControllerAdvice(basePackages = "at.fhv.eventmangement.presentation.controllers")
public class CurrentUserAdvice {
    @ModelAttribute("currentUser")
    public String currentUser(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }
}
