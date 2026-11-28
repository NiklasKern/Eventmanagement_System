package at.fhv.eventmangement.infrastructure.security;

import at.fhv.eventmangement.domain.model.UserAccount;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AccountUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public AccountUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);

        UserAccount account = userAccountRepository.findByUsername(normalized)
                .orElseThrow(() -> new UsernameNotFoundException("unknown user"));

        return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .disabled(!account.isEnabled())
                .authorities(account.getRole().getPermissions().stream()
                        .map(permission -> new SimpleGrantedAuthority(permission.name()))
                        .toList())
                .build();
    }
}