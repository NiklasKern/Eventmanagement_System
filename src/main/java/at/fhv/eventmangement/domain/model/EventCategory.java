package at.fhv.eventmangement.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Table(name = "event_category")
public class EventCategory extends BaseEntity{
    @Column(name = "category_name", unique = true)
    private String categoryName;
}
