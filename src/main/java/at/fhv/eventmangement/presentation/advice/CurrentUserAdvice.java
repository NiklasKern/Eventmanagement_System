package at.fhv.eventmangement.presentation.advice;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Set;
import java.util.stream.Collectors;

// Adds logged-in user into every page
@ControllerAdvice(basePackages = "at.fhv.eventmangement.presentation.controllers")
public class CurrentUserAdvice {

    @ModelAttribute("currentUser")
    public String currentUser(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }

    // Permission names of the logged-in user, e.g. {"EVENT_VIEW", "BOOK_EVENTS"}
    @ModelAttribute("permissions")
    public Set<String> permissions(Authentication authentication) {
        if (authentication == null) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}