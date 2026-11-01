package at.fhv.eventmangement.infrastructure.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Table(name = "organizer")
public class EventOrganizerEntity extends BaseEntity{
    @Embedded
    private ContactInfoEmbeddable contactInfo;
}