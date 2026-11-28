package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.application.dto.RoleDto;
import at.fhv.eventmangement.application.form.RoleForm;
import at.fhv.eventmangement.domain.exceptions.ProtectedRoleException;
import at.fhv.eventmangement.domain.exceptions.RoleAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.RoleInUseException;
import at.fhv.eventmangement.domain.exceptions.RoleNotFoundException;
import at.fhv.eventmangement.domain.model.Permissions;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    List<RoleDto> getAllRoles();

    RoleDto getRole(UUID id) throws RoleNotFoundException;

    List<Permissions> getAllPermissions();

    RoleDto createRole(@Valid RoleForm form) throws RoleAlreadyExistsException;

    RoleDto updateRole(UUID id, @Valid RoleForm form) throws RoleNotFoundException, RoleAlreadyExistsException, ProtectedRoleException;

    void deleteRole(UUID id) throws RoleInUseException, RoleNotFoundException, ProtectedRoleException;
}
