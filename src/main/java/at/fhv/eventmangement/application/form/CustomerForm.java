package at.fhv.eventmangement.application.form;

import at.fhv.eventmangement.domain.model.CustomerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerForm {

    @NotBlank(message = "please fill mandatory fields")
    private String name;

    @NotBlank(message = "please fill mandatory fields")
    private String address;

    @NotBlank(message = "please fill mandatory fields")
    private String email;

    private String phone;

    @NotNull(message = "please fill mandatory fields")
    private CustomerType customerType;
}
