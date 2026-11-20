package at.fhv.eventmangement.domain.model;

import at.fhv.eventmangement.domain.exceptions.ProtectedRoleException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Getter
@SuperBuilder
@AllArgsConstructor
public class Role extends BaseModel {
    // Role names
    public static final String ADMIN = "Admin";
    public static final String CUSTOMER = "Customer";
    public static final String EVENT_PROVIDER = "Event Provider";
    public static final String EVENT_MANAGEMENT = "Event Management";
    public static final String CUSTOMER_MANAGEMENT = "Customer Management";
    public static final String FINANCE_DEPARTMENT = "Finance Department";
    public static final String ON_PREM_CUSTOMER_MANAGEMENT = "On Prem Customer Management";

    // without this nobody can modify permissions
    private static final EnumSet<Permissions> ADMIN_MUST_KEEP = EnumSet.of(Permissions.MANAGE_ROLES, Permissions.MANAGE_USERS);

    private String name;
    private String description;

    @Builder.Default
    private Set<Permissions> permissions = EnumSet.noneOf(Permissions.class);

    // check if the role is builtIn, true for the roles above
    private boolean builtIn;

    public boolean hasPermission(Permissions permission) {
        return permissions.contains(permission);
    }

    public Set<Permissions> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public boolean isAdmin() {
        return ADMIN.equals(name);
    }

    public void changePermissions(Set<Permissions> newPermissions) throws ProtectedRoleException {
        if (isAdmin() && !newPermissions.containsAll(ADMIN_MUST_KEEP)) {
            throw new ProtectedRoleException("the Admin role must keep the permissions to manage roles and users");
        }
        permissions = newPermissions.isEmpty()
                ? EnumSet.noneOf(Permissions.class)
                : EnumSet.copyOf(newPermissions);
    }

    public void changeDetails(String newName, String newDescription) throws ProtectedRoleException {
        if (builtIn && !name.equals(newName)) {
            throw new ProtectedRoleException("built-in roles cannot be changed");
        }
        name = newName;
        description = newDescription;
    }
}