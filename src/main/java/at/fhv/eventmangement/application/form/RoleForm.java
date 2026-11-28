package at.fhv.eventmangement.application.form;

import at.fhv.eventmangement.domain.model.Permissions;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleForm {
    @NotBlank(message = "please fill mandatory fields")
    @Size(max = 50, message = "role name can not be longer than 50 characters")
    private String name;

    @Size(max = 255, message = "description can not be longer than 255 characters")
    private String description;

    @Builder.Default
    private Set<Permissions> permissions = new HashSet<>();
}
