package at.fhv.eventmangement.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder
@AllArgsConstructor
public class Event extends BaseModel{
    private String name;
    private String description;
    private String location;
    private BigDecimal price;
    private int minParticipants;
    private int maxParticipants;
    private EventStatus eventStatus;
    private EventCategory eventCategory;
    private EventOrganizer eventOrganizer;
    @Builder.Default
    private List<EventDate> eventDates = new ArrayList<>();
}