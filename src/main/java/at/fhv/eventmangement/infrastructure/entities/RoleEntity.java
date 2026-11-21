package at.fhv.eventmangement.infrastructure.entities;

import at.fhv.eventmangement.domain.model.Permissions;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Table(name = "role")
public class RoleEntity extends BaseEntity{
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    // Set of Enum values
    @ElementCollection(fetch = FetchType.EAGER) // permissions are needed every time a role is loaded
    @CollectionTable(name = "role_permission", joinColumns = @JoinColumn(name = "role_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    @Builder.Default
    private Set<Permissions> permissions = new HashSet<>();

    @Column(name = "built_in", nullable = false)
    private boolean builtIn;
}
