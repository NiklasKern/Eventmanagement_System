package at.fhv.eventmangement.application.form;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCategoryForm {
    @NotBlank(message = "please fill mandatory fields")
    private String categoryName;
}
