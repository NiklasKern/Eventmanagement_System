package at.fhv.eventmangement.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Event {
    private UUID id;
    private String name;
    private String description;
    private String location;
    private BigDecimal price;
    private int minParticipants;
    private int maxParticipants;
    private LocalDateTime dateTime;
    private EventStatus eventStatus;
    private EventCategory eventCategory;
    private EventOrganizer eventOrganizer;
    private LocalDateTime created;
    private LocalDateTime updated;
}
