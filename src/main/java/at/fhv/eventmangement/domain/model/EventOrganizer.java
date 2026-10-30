package at.fhv.eventmangement.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Table(name = "organizer")
public class EventOrganizer extends BaseEntity{
    @Embedded
    private ContactInfo contactInfo;
}
