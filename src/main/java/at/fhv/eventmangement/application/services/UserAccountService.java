package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.application.dto.UserAccountDto;
import at.fhv.eventmangement.application.form.PasswordChangeForm;
import at.fhv.eventmangement.application.form.UserAccountForm;
import at.fhv.eventmangement.domain.exceptions.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface UserAccountService {
    List<UserAccountDto> getAllAccounts();

    UserAccountDto getAccount(UUID id) throws UserAccountNotFoundException;

    UserAccountDto createAccount(@Valid UserAccountForm form)
            throws RoleNotFoundException, UserAccountAlreadyExistsException,
            CustomerNotFoundException, EventOrganizerNotFoundException;

    UserAccountDto changeRole(UUID accountId, UUID roleId)
            throws UserAccountNotFoundException, RoleNotFoundException, ProtectedRoleException;

    void disable(UUID accountId) throws UserAccountNotFoundException, ProtectedRoleException;

    void enable(UUID accountId) throws UserAccountNotFoundException;

    void changePassword(UUID accountId, @Valid PasswordChangeForm form) throws UserAccountNotFoundException;
}
