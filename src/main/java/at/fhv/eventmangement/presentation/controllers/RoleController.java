package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.RoleDto;
import at.fhv.eventmangement.application.form.RoleForm;
import at.fhv.eventmangement.application.services.RoleService;
import at.fhv.eventmangement.domain.exceptions.ProtectedRoleException;
import at.fhv.eventmangement.domain.exceptions.RoleAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.RoleInUseException;
import at.fhv.eventmangement.domain.exceptions.RoleNotFoundException;
import at.fhv.eventmangement.domain.model.Permissions;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/roles")
public class RoleController {

    private static final String LIST = "role_list";
    private static final String FORM = "role_form";
    private static final String REDIRECT_LIST = "redirect:/admin/roles";

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    // Runs before every method of this controller
    @ModelAttribute("allPermissions")
    public List<Permissions> allPermissions() {
        return roleService.getAllPermissions();
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("roles", roleService.getAllRoles());
        return LIST;
    }

    @GetMapping("/new")
    public String newRole(Model model) {
        model.addAttribute("role", new RoleForm());
        return FORM;
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("role") RoleForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return FORM;
        }
        try {
            roleService.createRole(form);
        } catch (RoleAlreadyExistsException ex) {
            bindingResult.rejectValue("name", "duplicate", ex.getMessage());
            return FORM;
        }
        redirectAttributes.addFlashAttribute("successMessage", "Role '" + form.getName().trim() + "' created");
        return REDIRECT_LIST;
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            RoleDto role = roleService.getRole(id);
            model.addAttribute("role", toForm(role));
            addEditInfo(model, role);
            return FORM;
        } catch (RoleNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return REDIRECT_LIST;
        }
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable UUID id,
                         @Valid @ModelAttribute("role") RoleForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            addEditInfo(model, roleService.getRole(id));   // needed if the form is shown again
            if (bindingResult.hasErrors()) {
                return FORM;
            }
            roleService.updateRole(id, form);
        } catch (RoleNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return REDIRECT_LIST;
        } catch (RoleAlreadyExistsException ex) {
            bindingResult.rejectValue("name", "duplicate", ex.getMessage());
            return FORM;
        } catch (ProtectedRoleException ex) {
            // e.g. Admin without MANAGE_ROLES, or renaming a built-in role
            bindingResult.reject("protected", ex.getMessage());
            return FORM;
        }
        redirectAttributes.addFlashAttribute("successMessage",
                "Role saved. Users with this role get the change at their next login.");
        return REDIRECT_LIST;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            roleService.deleteRole(id);
            redirectAttributes.addFlashAttribute("successMessage", "Role deleted");
        } catch (RoleNotFoundException | RoleInUseException | ProtectedRoleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return REDIRECT_LIST;
    }

    private static void addEditInfo(Model model, RoleDto role) {
        model.addAttribute("roleId", role.id());
        model.addAttribute("builtIn", role.builtIn());
    }

    private static RoleForm toForm(RoleDto role) {
        return RoleForm.builder()
                .name(role.name())
                .description(role.description())
                .permissions(new HashSet<>(role.permissions()))
                .build();
    }
}