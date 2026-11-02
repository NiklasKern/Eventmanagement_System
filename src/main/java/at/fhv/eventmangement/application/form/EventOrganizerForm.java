package at.fhv.eventmangement.application.form;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventOrganizerForm {

    @NotBlank(message = "please fill mandatory fields")
    public String organizerName;

    @NotBlank(message = "please fill mandatory fields")
    public String address;

    @NotBlank(message = "please fill mandatory fields")
    public String email;
}
