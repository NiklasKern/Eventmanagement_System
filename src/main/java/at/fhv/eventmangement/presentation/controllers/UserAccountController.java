package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.CustomerDto;
import at.fhv.eventmangement.application.dto.EventOrganizerDto;
import at.fhv.eventmangement.application.dto.RoleDto;
import at.fhv.eventmangement.application.dto.UserAccountDto;
import at.fhv.eventmangement.application.form.PasswordChangeForm;
import at.fhv.eventmangement.application.form.UserAccountForm;
import at.fhv.eventmangement.application.services.CustomerService;
import at.fhv.eventmangement.application.services.EventOrganizerService;
import at.fhv.eventmangement.application.services.RoleService;
import at.fhv.eventmangement.application.services.UserAccountService;
import at.fhv.eventmangement.domain.exceptions.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/users")
public class UserAccountController {

    private static final String LIST = "user_list";
    private static final String FORM = "user_form";
    private static final String PASSWORD_FORM = "user_password";
    private static final String REDIRECT_LIST = "redirect:/admin/users";

    private final UserAccountService userAccountService;
    private final RoleService roleService;
    private final CustomerService customerService;
    private final EventOrganizerService eventOrganizerService;

    public UserAccountController(UserAccountService userAccountService, RoleService roleService,
                                 CustomerService customerService, EventOrganizerService eventOrganizerService) {
        this.userAccountService = userAccountService;
        this.roleService = roleService;
        this.customerService = customerService;
        this.eventOrganizerService = eventOrganizerService;
    }

    // Role dropdowns on the list and the form
    @ModelAttribute("roles")
    public List<RoleDto> roles() {
        return roleService.getAllRoles();
    }

    // "Linked customer" dropdown on the form
    @ModelAttribute("customers")
    public List<CustomerDto> customers() {
        return customerService.getAllCustomers();
    }

    // "Linked event organizer" dropdown on the form
    @ModelAttribute("organizers")
    public List<EventOrganizerDto> organizers() {
        return eventOrganizerService.getAllOrganizers();
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("accounts", userAccountService.getAllAccounts());
        return LIST;
    }

    @GetMapping("/new")
    public String newAccount(Model model) {
        model.addAttribute("account", new UserAccountForm());
        return FORM;
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("account") UserAccountForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return FORM;
        }
        try {
            userAccountService.createAccount(form);
        } catch (UserAccountAlreadyExistsException ex) {
            bindingResult.rejectValue("username", "duplicate", ex.getMessage());
            return FORM;
        } catch (RoleNotFoundException ex) {
            bindingResult.rejectValue("roleId", "unknown", ex.getMessage());
            return FORM;
        } catch (CustomerNotFoundException | EventOrganizerNotFoundException ex) {
            bindingResult.reject("link", ex.getMessage());
            return FORM;
        }
        redirectAttributes.addFlashAttribute("successMessage", "Account '" + form.getUsername() + "' created");
        return REDIRECT_LIST;
    }

    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable UUID id, @RequestParam UUID roleId,
                             RedirectAttributes redirectAttributes) {
        try {
            UserAccountDto account = userAccountService.changeRole(id, roleId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Role of '" + account.username() + "' changed to " + account.roleName());
        } catch (UserAccountNotFoundException | RoleNotFoundException | ProtectedRoleException ex) {
            // ProtectedRoleException: the last admin cannot get another role
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return REDIRECT_LIST;
    }

    @PostMapping("/{id}/disable")
    public String disable(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            userAccountService.disable(id);
            redirectAttributes.addFlashAttribute("successMessage", "Account disabled");
        } catch (UserAccountNotFoundException | ProtectedRoleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return REDIRECT_LIST;
    }

    @PostMapping("/{id}/enable")
    public String enable(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            userAccountService.enable(id);
            redirectAttributes.addFlashAttribute("successMessage", "Account enabled");
        } catch (UserAccountNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return REDIRECT_LIST;
    }

    @GetMapping("/{id}/password")
    public String passwordForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("account", userAccountService.getAccount(id));
        } catch (UserAccountNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return REDIRECT_LIST;
        }
        model.addAttribute("passwordForm", new PasswordChangeForm());
        return PASSWORD_FORM;
    }

    @PostMapping("/{id}/password")
    public String changePassword(@PathVariable UUID id,
                                 @Valid @ModelAttribute("passwordForm") PasswordChangeForm form,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            UserAccountDto account = userAccountService.getAccount(id);
            if (bindingResult.hasErrors()) {
                model.addAttribute("account", account);
                return PASSWORD_FORM;
            }
            userAccountService.changePassword(id, form);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Password of '" + account.username() + "' changed");
        } catch (UserAccountNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return REDIRECT_LIST;
    }
}