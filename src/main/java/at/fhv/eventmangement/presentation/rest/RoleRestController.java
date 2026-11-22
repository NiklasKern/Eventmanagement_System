package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.RoleDto;
import at.fhv.eventmangement.application.form.RoleForm;
import at.fhv.eventmangement.application.services.RoleService;
import at.fhv.eventmangement.domain.exceptions.ProtectedRoleException;
import at.fhv.eventmangement.domain.exceptions.RoleAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.RoleInUseException;
import at.fhv.eventmangement.domain.exceptions.RoleNotFoundException;
import at.fhv.eventmangement.domain.model.Permissions;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/roles")
public class RoleRestController {

    private final RoleService roleService;

    public RoleRestController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<RoleDto> getRoles() {
        return roleService.getAllRoles();
    }

    @GetMapping("/permissions")
    public List<Permissions> getPermissions() {
        return roleService.getAllPermissions();
    }

    @GetMapping("/{id}")
    public RoleDto getRole(@PathVariable UUID id) throws RoleNotFoundException {
        return roleService.getRole(id);
    }

    // 201 Created + Location header pointing to the new role
    @PostMapping
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleForm form) throws RoleAlreadyExistsException {
        RoleDto created = roleService.createRole(form);
        return ResponseEntity.created(URI.create("/api/admin/roles/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public RoleDto updateRole(@PathVariable UUID id, @Valid @RequestBody RoleForm form)
            throws RoleNotFoundException, RoleAlreadyExistsException, ProtectedRoleException {
        return roleService.updateRole(id, form);
    }

    // 204 No Content: deleted, nothing to send back
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id)
            throws RoleNotFoundException, RoleInUseException, ProtectedRoleException {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}