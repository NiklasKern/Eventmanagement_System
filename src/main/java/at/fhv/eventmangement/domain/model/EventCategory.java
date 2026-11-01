package at.fhv.eventmangement.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
public class EventCategory extends BaseModel {
    private String categoryName;
}