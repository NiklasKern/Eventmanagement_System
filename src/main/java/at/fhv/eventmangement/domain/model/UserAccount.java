package at.fhv.eventmangement.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SuperBuilder;

import java.util.Objects;

@Getter
@SuperBuilder
@AllArgsConstructor
public class UserAccount extends BaseModel {
    private String username;
    private String passwordHash;
    private boolean enabled; //lets admin block an account
    private Role role;

    private Customer customer;
    private EventOrganizer organizer;

    public boolean hasPermission(Permissions permissions) {
        return enabled && role != null && role.hasPermission(permissions);
    }

    public void changeRole(Role newRole) {
        this.role = Objects.requireNonNull(newRole, "Role must not be null");
    }

    public void changePasswordHash(String newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash, "Password can not be null");
    }

    public boolean canEdit(Event event) {
        if (hasPermission(Permissions.EDIT_ALL_EVENTS)) {
            return true;
        }
        return hasPermission(Permissions.EDIT_OWN_EVENTS)
                && organizer != null
                && event.getEventOrganizer() != null
                && organizer.getId().equals(event.getEventOrganizer().getId());
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }
}
