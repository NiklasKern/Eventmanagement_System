package at.fhv.eventmangement.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
public class EventOrganizer extends BaseModel{
    private ContactInfo contactInfo;
}