package at.fhv.eventmangement.infrastructure.entities;

import at.fhv.eventmangement.domain.model.CustomerType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Table(name = "customer")
public class CustomerEntity extends BaseEntity {

    @Embedded
    private ContactInfoEmbeddable contactInfo;

    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    private CustomerType customerType;
}
