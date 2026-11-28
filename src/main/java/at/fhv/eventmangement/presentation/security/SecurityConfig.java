package at.fhv.eventmangement.presentation.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static at.fhv.eventmangement.domain.model.Permissions.*;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Order matters: the FIRST matching rule wins, so specific URLs come before general ones
                .authorizeHttpRequests(auth -> auth
                        // public
                        .requestMatchers("/login", "/styles/**", "/images/**", "/error").permitAll()

                        // every logged-in user: main page (its sections depend on the permissions)
                        .requestMatchers("/", "/home", "/access-denied").authenticated()

                        // organizer pages
                        .requestMatchers("/event/manage/create").hasAuthority(EVENT_CREATE.name())
                        .requestMatchers("/event/manage/*/modify", "/event/manage/*/cancel")
                        .hasAnyAuthority(EDIT_OWN_EVENTS.name(), EDIT_ALL_EVENTS.name())
                        .requestMatchers("/events")
                        .hasAnyAuthority(EVENT_CREATE.name(), EDIT_OWN_EVENTS.name(), EDIT_ALL_EVENTS.name())

                        // customer pages: booking before the general /customer rule
                        .requestMatchers("/customer/event/*/book").hasAuthority(BOOK_EVENTS.name())
                        .requestMatchers("/customer/event/*/invoice-preview", "/customer/invoice/**").hasAuthority(BOOK_EVENTS.name())
                        .requestMatchers("/customer/bookings", "/customer/booking/**").hasAuthority(VIEW_OWN_BOOKINGS.name())
                        .requestMatchers("/customer/**").hasAuthority(EVENT_VIEW.name())

                        // REST API
                        .requestMatchers("/api/customer/invoice/**").hasAuthority(BOOK_EVENTS.name())
                        .requestMatchers(HttpMethod.PUT, "/api/events/*")
                        .hasAnyAuthority(EDIT_OWN_EVENTS.name(), EDIT_ALL_EVENTS.name())
                        .requestMatchers(HttpMethod.GET, "/api/events/**", "/api/customer/**")
                        .hasAuthority(EVENT_VIEW.name())

                        // role and user management
                        .requestMatchers("/admin/roles/**", "/api/admin/roles/**").hasAuthority(MANAGE_ROLES.name())
                        .requestMatchers("/admin/users/**", "/api/admin/users/**").hasAuthority(MANAGE_USERS.name())

                        // everything not listed above is forbidden
                        .anyRequest().denyAll())

                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .permitAll())

                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())

                // logged in, but missing the permission → our own page instead of a bare 403
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"))

                .httpBasic(Customizer.withDefaults())

                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }
}