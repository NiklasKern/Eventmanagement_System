package at.fhv.eventmangement.domain.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EventForm {

    @NotBlank
    String name;

    String description;

    @NotEmpty
    String location;

    @Future(message = "Date has to be in the future")
    LocalDateTime dateTime;

    @Positive
    Float price;

    @Positive
    Integer capacity;

    @NotEmpty
    String eventCategory;

    public EventForm setName(String name) {
        this.name = name;
        return this;
    }

    public EventForm setDescription(String description) {
        this.description = description;
        return this;
    }

    public EventForm setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public EventForm setPrice(Float price) {
        this.price = price;
        return this;
    }

    public EventForm setCapacity(Integer capacity) {
        this.capacity = capacity;
        return this;
    }

    public EventForm setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
        return this;
    }
}
