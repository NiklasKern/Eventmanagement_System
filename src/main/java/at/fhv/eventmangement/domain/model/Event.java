package at.fhv.eventmangement.domain.model;

import at.fhv.eventmangement.domain.exceptions.EventAlreadyCancelledException;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
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
    public void cancel() throws EventAlreadyCancelledException {
        if (this.eventStatus == EventStatus.CANCELLED) {
            throw new EventAlreadyCancelledException();
        }
        this.eventStatus = EventStatus.CANCELLED;
    }

    public boolean isCancelled() {
        return this.eventStatus == EventStatus.CANCELLED;
    }
}