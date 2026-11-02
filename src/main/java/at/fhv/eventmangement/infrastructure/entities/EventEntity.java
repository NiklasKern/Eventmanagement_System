package at.fhv.eventmangement.infrastructure.entities;

import at.fhv.eventmangement.domain.model.EventStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "min_participants", nullable = false)
    private int minParticipants;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", nullable = false)
    private EventStatus eventStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private EventCategoryEntity eventCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_organizer_id")
    private EventOrganizerEntity eventOrganizer;

    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime created;

    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
}