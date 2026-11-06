package at.fhv.eventmangement.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
public class Customer extends BaseModel {
    private ContactInfo contactInfo;
    private String phone;
    private CustomerType customerType;
}