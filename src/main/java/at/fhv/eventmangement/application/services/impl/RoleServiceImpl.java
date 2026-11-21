package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.dto.RoleDto;
import at.fhv.eventmangement.application.form.RoleForm;
import at.fhv.eventmangement.application.mappers.RoleMapper;
import at.fhv.eventmangement.application.services.RoleService;
import at.fhv.eventmangement.domain.exceptions.ProtectedRoleException;
import at.fhv.eventmangement.domain.exceptions.RoleAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.RoleInUseException;
import at.fhv.eventmangement.domain.exceptions.RoleNotFoundException;
import at.fhv.eventmangement.domain.model.Permissions;
import at.fhv.eventmangement.domain.model.Role;
import at.fhv.eventmangement.domain.repositories.RoleRepository;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Validated
@Service
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final UserAccountRepository userAccountRepository;
    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleRepository roleRepository,
                           UserAccountRepository userAccountRepository,
                           RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
        this.roleRepository = roleRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream().map(roleMapper::toDto).toList();
    }

    @Override
    public RoleDto getRole(UUID id) throws RoleNotFoundException {
        return roleMapper.toDto(findRole(id));
    }

    @Override
    public List<Permissions> getAllPermissions() {
        return List.of(Permissions.values());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleDto createRole(RoleForm form) throws RoleAlreadyExistsException {
        String name = form.getName().trim();
        if (roleRepository.existsByNameIgnoreCase(name)) {
            throw new RoleAlreadyExistsException();
        }

        Role role = Role.builder()
                .name(name)
                .description(blankToNull(form.getDescription()))
                .permissions(toEnumSet(form.getPermissions()))
                .builtIn(false)
                .build();

        return roleMapper.toDto(roleRepository.save(role));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleDto updateRole(UUID id, RoleForm form) throws RoleNotFoundException, RoleAlreadyExistsException, ProtectedRoleException {
        Role role = findRole(id);
        String name = form.getName().trim();

        if (!role.getName().equalsIgnoreCase(name) && roleRepository.existsByNameIgnoreCase(name)) {
            throw new RoleAlreadyExistsException();
        }

        role.changeDetails(name, blankToNull(form.getDescription()));
        role.changePermissions(toEnumSet(form.getPermissions()));

        return roleMapper.toDto(roleRepository.save(role));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(UUID id) throws ProtectedRoleException, RoleNotFoundException, RoleInUseException {
        Role role = findRole(id);
        if (role.isBuiltIn()) {
            throw new ProtectedRoleException("built-in roles cant be deleted");
        }
        if (userAccountRepository.countByRoleId(id) > 0) {
            throw new RoleInUseException();
        }
        roleRepository.deleteById(id);
    }

    private Role findRole(UUID id) throws RoleNotFoundException {
        return roleRepository.findById(id).orElseThrow(RoleNotFoundException::new);
    }

    private static Set<Permissions> toEnumSet(Set<Permissions> permissions) {
        return permissions == null || permissions.isEmpty()
                ? EnumSet.noneOf(Permissions.class)
                : EnumSet.copyOf(permissions);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
