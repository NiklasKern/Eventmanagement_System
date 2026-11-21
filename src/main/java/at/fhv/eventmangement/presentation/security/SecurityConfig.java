package at.fhv.eventmangement.presentation.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/styles/**", "/images/**", "/error").permitAll() //can reach without login
                        .anyRequest().authenticated()) // rest

                // login pages
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .permitAll())

                // /logout ends sesstion
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())

                // Needed in case we call it outside the browser
                .httpBasic(Customizer.withDefaults())

                // protects from hidden form attacks
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }
}
