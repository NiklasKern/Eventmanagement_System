package at.fhv.eventmangement.domain.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Who may change which event (UserAccount.canEdit)
class UserAccountTest {

    private final EventOrganizer mine = EventOrganizer.builder().id(UUID.randomUUID()).build();
    private final EventOrganizer other = EventOrganizer.builder().id(UUID.randomUUID()).build();

    private static UserAccount account(EventOrganizer organizer, boolean enabled, Permissions... permissions) {
        Role role = Role.builder().name("Test").permissions(EnumSet.of(permissions[0], permissions)).build();
        return UserAccount.builder().username("anna").enabled(enabled).role(role).organizer(organizer).build();
    }

    private static Event eventOf(EventOrganizer organizer) {
        return Event.builder().name("Hike").eventOrganizer(organizer).build();
    }

    @Test
    void editAll_canEditEveryEvent() {
        assertThat(account(null, true, Permissions.EDIT_ALL_EVENTS).canEdit(eventOf(other))).isTrue();
    }

    @Test
    void editOwn_canEditOwnEvent() {
        assertThat(account(mine, true, Permissions.EDIT_OWN_EVENTS).canEdit(eventOf(mine))).isTrue();
    }

    @Test
    void editOwn_cannotEditForeignEvent() {
        assertThat(account(mine, true, Permissions.EDIT_OWN_EVENTS).canEdit(eventOf(other))).isFalse();
    }

    @Test
    void editOwn_comparesIdsNotObjects() {
        EventOrganizer sameOrganizerLoadedAgain = EventOrganizer.builder().id(mine.getId()).build();

        assertThat(account(mine, true, Permissions.EDIT_OWN_EVENTS).canEdit(eventOf(sameOrganizerLoadedAgain))).isTrue();
    }

    @Test
    void editOwn_withoutLinkedOrganizer_cannotEdit() {
        assertThat(account(null, true, Permissions.EDIT_OWN_EVENTS).canEdit(eventOf(mine))).isFalse();
    }

    @Test
    void editOwn_eventWithoutOrganizer_cannotEdit() {
        assertThat(account(mine, true, Permissions.EDIT_OWN_EVENTS).canEdit(eventOf(null))).isFalse();
    }

    @Test
    void withoutEditPermission_cannotEdit() {
        assertThat(account(mine, true, Permissions.EVENT_CREATE).canEdit(eventOf(mine))).isFalse();
    }

    @Test
    void disabledAccount_cannotEdit() {
        assertThat(account(mine, false, Permissions.EDIT_ALL_EVENTS).canEdit(eventOf(mine))).isFalse();
    }
}