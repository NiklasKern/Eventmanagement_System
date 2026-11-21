package at.fhv.eventmangement.infrastructure.security;

import at.fhv.eventmangement.domain.model.Permissions;
import at.fhv.eventmangement.domain.model.Role;
import at.fhv.eventmangement.domain.model.UserAccount;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.EnumSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountUserDetailsServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private AccountUserDetailsService service;

    private static UserAccount anna(boolean enabled) {
        Role customer = Role.builder().name(Role.CUSTOMER)
                .permissions(EnumSet.of(Permissions.EVENT_VIEW, Permissions.BOOK_EVENTS)).build();
        return UserAccount.builder().username("anna").passwordHash("$argon2id$hash")
                .enabled(enabled).role(customer).build();
    }

    @Test
    void loadsAccount_withPermissionsAsAuthorities() {
        when(userAccountRepository.findByUsername("anna")).thenReturn(Optional.of(anna(true)));

        UserDetails user = service.loadUserByUsername("anna");

        assertThat(user.getUsername()).isEqualTo("anna");
        assertThat(user.getPassword()).isEqualTo("$argon2id$hash");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("EVENT_VIEW", "BOOK_EVENTS");
    }

    @Test
    void usernameIsNormalized_likeWhenTheAccountWasCreated() {
        when(userAccountRepository.findByUsername("anna")).thenReturn(Optional.of(anna(true)));

        assertThat(service.loadUserByUsername("  Anna ").getUsername()).isEqualTo("anna");
    }

    @Test
    void disabledAccount_isMarkedDisabled() {
        when(userAccountRepository.findByUsername("anna")).thenReturn(Optional.of(anna(false)));

        assertThat(service.loadUserByUsername("anna").isEnabled()).isFalse();
    }

    @Test
    void unknownUser_throwsUsernameNotFound() {
        when(userAccountRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}