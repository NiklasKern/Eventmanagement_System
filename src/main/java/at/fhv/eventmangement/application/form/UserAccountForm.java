package at.fhv.eventmangement.application.form;

import at.fhv.eventmangement.application.validation.StrongPassword;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccountForm {
    @NotBlank(message = "username can not be empty")
    @Size(min = 3, max = 25, message = "username has to be between 3 and 25 characters long")
    @Pattern(regexp = "^[A-Za-z0-9._-]*$",
            message = "username may only contain letters, numbers, '.', '_' and '-'")
    private String username;

    @StrongPassword
    private String password;

    @NotBlank(message = "please fill mandatory fields")
    private String confirmPassword;

    @NotNull(message = "please fill mandatory fields")
    private UUID roleId;

    private UUID customerId;
    private UUID organizerId;

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordConfirmed() {
        return password == null || password.equals(confirmPassword);
    }
}
