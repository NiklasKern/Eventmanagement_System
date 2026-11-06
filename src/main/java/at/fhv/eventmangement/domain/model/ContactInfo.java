package at.fhv.eventmangement.domain.model;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class ContactInfo {
    private String name;
    private String address;
    private String email;
}