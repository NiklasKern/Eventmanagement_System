package at.fhv.eventmangement.application.form;

import at.fhv.eventmangement.application.validation.StrongPassword;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordChangeForm {
    @StrongPassword
    private String newPassword;

    @NotBlank(message = "please fill mandatory fields")
    private String confirmedPassword;

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordConfirmed() {
        return newPassword == null || newPassword.equals(confirmedPassword);
    }
}
