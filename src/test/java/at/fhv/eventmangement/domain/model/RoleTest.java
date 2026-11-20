package at.fhv.eventmangement.domain.model;

import at.fhv.eventmangement.domain.exceptions.ProtectedRoleException;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleTest {

    @Test
    void adminMustKeepRoleAndUserManagement() {
        Role admin = Role.builder().name(Role.ADMIN).builtIn(true)
                .permissions(EnumSet.allOf(Permissions.class)).build();

        assertThatThrownBy(() -> admin.changePermissions(EnumSet.of(Permissions.EVENT_VIEW)))
                .isInstanceOf(ProtectedRoleException.class);
    }

    @Test
    void builtInRoleCannotBeRenamed() {
        Role customer = Role.builder().name(Role.CUSTOMER).builtIn(true).build();

        assertThatThrownBy(() -> customer.changeDetails("Client", "x"))
                .isInstanceOf(ProtectedRoleException.class);
    }

    @Test
    void disabledAccountHasNoPermissions() {
        Role customer = Role.builder().name(Role.CUSTOMER)
                .permissions(EnumSet.of(Permissions.BOOK_EVENTS)).build();
        UserAccount account = UserAccount.builder().username("anna").role(customer).enabled(false).build();

        assertThat(account.hasPermission(Permissions.BOOK_EVENTS)).isFalse();
    }
}