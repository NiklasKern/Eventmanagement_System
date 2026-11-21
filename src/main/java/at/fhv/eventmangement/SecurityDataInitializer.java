package at.fhv.eventmangement;

import at.fhv.eventmangement.application.services.SecuritySetupService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class SecurityDataInitializer implements ApplicationRunner {
    private final SecuritySetupService securitySetupService;
    private final String adminUsername;
    private final String adminPassword;

    public SecurityDataInitializer(SecuritySetupService securitySetupService,
                                @Value("${app.security.admin.username:admin}") String adminUsername,
                                @Value("${app.security.admin.password:}") String adminPassword) {
        this.adminPassword = adminPassword;
        this.adminUsername = adminUsername;
        this.securitySetupService = securitySetupService;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        securitySetupService.ensureBuiltInRoles();
        securitySetupService.ensureAdminAccount(adminUsername, adminPassword);
    }
}
