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
@Table(name = "event_category")
public class EventCategoryEntity extends BaseEntity{
    @Column(name = "category_name", unique = true)
    private String categoryName;
}