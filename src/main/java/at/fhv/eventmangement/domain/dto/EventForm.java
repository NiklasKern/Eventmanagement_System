package at.fhv.eventmangement.domain.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EventForm {

    @NotBlank(message = "please fill mandatory fields")
    private String name;

    private String description;

    @NotBlank(message = "please fill mandatory fields")
    private String location;

    @NotNull(message = "please fill mandatory fields")
    @Future(message = "Date has to be in the future")
    private LocalDateTime dateTime;

    @Positive(message = "must be greater than 0")
    private Float price;

    @Positive(message = "must be greater than 0")
    private Integer capacity;

    @NotBlank(message = "please fill mandatory fields")
    private String eventCategory;

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

    public EventForm setLocation(String location) {
        this.location = location;
        return this;
    }
}
