package at.fhv.eventmangement.application.form;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItemForm {

    @NotBlank(message = "please fill mandatory fields")
    private String description;

    @NotNull(message = "please fill mandatory fields")
    @Positive(message = "must be greater than 0")
    private Integer quantity;

    @NotNull(message = "please fill mandatory fields")
    @PositiveOrZero(message = "must not be negative")
    private BigDecimal unitPrice;

    @NotNull(message = "please fill mandatory fields")
    @Min(value = 0, message = "must be between 0 and 100")
    @Max(value = 100, message = "must be between 0 and 100")
    private Integer vatRate;
}
