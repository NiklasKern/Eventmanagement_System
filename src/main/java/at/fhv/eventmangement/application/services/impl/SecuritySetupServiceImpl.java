package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.form.UserAccountForm;
import at.fhv.eventmangement.application.services.SecuritySetupService;
import at.fhv.eventmangement.application.services.UserAccountService;
import at.fhv.eventmangement.domain.exceptions.EventManagementException;
import at.fhv.eventmangement.domain.exceptions.RoleNotFoundException;
import at.fhv.eventmangement.domain.model.Permissions;
import at.fhv.eventmangement.domain.model.Role;
import at.fhv.eventmangement.domain.repositories.RoleRepository;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static at.fhv.eventmangement.domain.model.Permissions.*;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SecuritySetupServiceImpl implements SecuritySetupService {
    private static final Map<String, Set<Permissions>> BUILT_IN_ROLES = Map.of(
            Role.ADMIN, EnumSet.allOf(Permissions.class),
            Role.CUSTOMER, EnumSet.of(EVENT_VIEW, BOOK_EVENTS, VIEW_OWN_BOOKINGS),
            Role.EVENT_PROVIDER, EnumSet.of(EVENT_VIEW, EVENT_CREATE, EDIT_OWN_EVENTS),
            Role.EVENT_MANAGEMENT, EnumSet.of(EVENT_VIEW, EVENT_CREATE, EDIT_ALL_EVENTS,
                    MANAGE_CATEGORIES, MANAGE_EVENT_PROVIDERS),
            Role.CUSTOMER_MANAGEMENT, EnumSet.of(VIEW_CUSTOMERS, MANAGE_CUSTOMERS, VIEW_ALL_BOOKINGS),
            Role.ON_PREM_CUSTOMER_MANAGEMENT, EnumSet.of(EVENT_VIEW, VIEW_CUSTOMERS, MANAGE_CUSTOMERS,
                    BOOK_FOR_CUSTOMER),
            Role.FINANCE_DEPARTMENT, EnumSet.of(VIEW_ALL_BOOKINGS, VIEW_PAYMENTS, MANAGE_PAYMENTS)
    );

    private final RoleRepository roleRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserAccountService userAccountService;

    public SecuritySetupServiceImpl(RoleRepository roleRepository, UserAccountRepository userAccountRepository, UserAccountService userAccountService) {
        this.roleRepository = roleRepository;
        this.userAccountRepository = userAccountRepository;
        this.userAccountService = userAccountService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ensureBuiltInRoles() {
        BUILT_IN_ROLES.forEach((name, permissions) -> {
            if (!roleRepository.existsByNameIgnoreCase(name)) {
                roleRepository.save(Role.builder()
                        .name(name)
                        .description("Built-in role")
                        .permissions(EnumSet.copyOf(permissions))
                        .builtIn(true)
                        .build());
                log.info("Created built-in for '{}'", name);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ensureAdminAccount(String username, String password) throws EventManagementException {
        Role admin = roleRepository.findByName(Role.ADMIN).orElseThrow(RoleNotFoundException::new);

        if (userAccountRepository.countEnabledByRoleId(admin.getId()) > 0) {
            return;
        }

        if (password == null || password.isBlank()) {
            throw new IllegalStateException("No admin account exists and no admin password is configured. "
                    + "Set app.security.admin.password (or the environment variable APP_SECURITY_ADMIN_PASSWORD).");
        }

        userAccountService.createAccount(UserAccountForm.builder()
                .username(username)
                .password(password)
                .confirmPassword(password)
                .roleId(admin.getId())
                .build());
        log.info("Created admin account '{}'", username);
    }
}
