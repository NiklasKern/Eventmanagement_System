package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.dto.UserAccountDto;
import at.fhv.eventmangement.application.form.PasswordChangeForm;
import at.fhv.eventmangement.application.form.UserAccountForm;
import at.fhv.eventmangement.application.mappers.UserAccountMapper;
import at.fhv.eventmangement.application.services.PasswordHasher;
import at.fhv.eventmangement.application.services.UserAccountService;
import at.fhv.eventmangement.domain.exceptions.*;
import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.model.EventOrganizer;
import at.fhv.eventmangement.domain.model.Role;
import at.fhv.eventmangement.domain.model.UserAccount;
import at.fhv.eventmangement.domain.repositories.CustomerRepository;
import at.fhv.eventmangement.domain.repositories.EventOrganizerRepository;
import at.fhv.eventmangement.domain.repositories.RoleRepository;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Validated
@Service
@Transactional(readOnly = true)
public class UserAccountServiceImpl implements UserAccountService {
    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final EventOrganizerRepository eventOrganizerRepository;
    private final PasswordHasher passwordHasher;
    private final UserAccountMapper userAccountMapper;

    public UserAccountServiceImpl(UserAccountRepository userAccountRepository,
                                  RoleRepository roleRepository,
                                  CustomerRepository customerRepository,
                                  EventOrganizerRepository eventOrganizerRepository,
                                  PasswordHasher passwordHasher,
                                  UserAccountMapper userAccountMapper) {
        this.customerRepository = customerRepository;
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.eventOrganizerRepository = eventOrganizerRepository;
        this.passwordHasher = passwordHasher;
        this.userAccountMapper = userAccountMapper;
    }

    @Override
    public List<UserAccountDto> getAllAccounts() {
        return userAccountRepository.findAll().stream().map(userAccountMapper::toDto).toList();
    }

    @Override
    public UserAccountDto getAccount(UUID id) throws UserAccountNotFoundException {
        return userAccountMapper.toDto(findAccount(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAccountDto createAccount(UserAccountForm form) throws UserAccountAlreadyExistsException, RoleNotFoundException,
            CustomerNotFoundException, EventOrganizerNotFoundException {

        String username = normalize(form.getUsername());

        if (userAccountRepository.existsByUsernameIgnoreCase(username)) {
            throw new UserAccountAlreadyExistsException();
        }

        Role role = roleRepository.findById(form.getRoleId()).orElseThrow(RoleNotFoundException::new);

        Customer customer = form.getCustomerId() == null ? null : customerRepository.findById(form.getCustomerId())
                .orElseThrow(CustomerNotFoundException::new);

        EventOrganizer organizer = form.getOrganizerId() == null ? null : eventOrganizerRepository.findById(form.getOrganizerId())
                .orElseThrow(EventOrganizerNotFoundException::new);

        UserAccount account = UserAccount.builder()
                .username(username)
                .passwordHash(passwordHasher.hash(form.getPassword()))
                .enabled(true)
                .role(role)
                .customer(customer)
                .organizer(organizer)
                .build();

        return userAccountMapper.toDto(userAccountRepository.save(account));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAccountDto changeRole(UUID accountId, UUID roleId) throws UserAccountNotFoundException, RoleNotFoundException, ProtectedRoleException {
        UserAccount account = findAccount(accountId);
        Role newRole = roleRepository.findById(roleId).orElseThrow(RoleNotFoundException::new);

        if (!newRole.isAdmin()) {
            ensureNotLastAdmin(account);
        }
        account.changeRole(newRole);

        return userAccountMapper.toDto(userAccountRepository.save(account));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(UUID accountId) throws UserAccountNotFoundException, ProtectedRoleException {
        UserAccount account = findAccount(accountId);
        ensureNotLastAdmin(account);
        account.disable();
        userAccountRepository.save(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(UUID accountId) throws UserAccountNotFoundException {
        UserAccount account = findAccount(accountId);
        account.enable();
        userAccountRepository.save(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(UUID accountId, PasswordChangeForm form) throws UserAccountNotFoundException {
        UserAccount account = findAccount(accountId);
        account.changePasswordHash(passwordHasher.hash(form.getNewPassword()));
        userAccountRepository.save(account);
    }

    private void ensureNotLastAdmin(UserAccount account) throws ProtectedRoleException {
        boolean isEnabledAdmin = account.isEnabled() && account.getRole().isAdmin();
        if (isEnabledAdmin && userAccountRepository.countEnabledByRoleId(account.getRole().getId()) <= 1) {
            throw new ProtectedRoleException("Last admin can not be disabled or given another role");
        }
    }

    private UserAccount findAccount(UUID id) throws UserAccountNotFoundException {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new UserAccountNotFoundException("User account does not exist"));
    }

    private static String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
