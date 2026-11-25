package at.fhv.eventmangement.application.form;

import at.fhv.eventmangement.domain.model.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceForm {

    @NotBlank(message = "please fill mandatory fields")
    private String name;

    @NotBlank(message = "please fill mandatory fields")
    private String address;

    @NotBlank(message = "please fill mandatory fields")
    @Email(message = "please enter a valid email address")
    private String email;

    @NotNull(message = "please fill mandatory fields")
    private PaymentMethod paymentMethod;
}