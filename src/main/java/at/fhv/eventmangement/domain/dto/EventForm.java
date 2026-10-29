package at.fhv.eventmangement.domain.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;

@Getter
public class EventForm {

    // Validation messages
    public static final String MANDATORY = "please fill mandatory fields";
    public static final String POSITIVE = "must be greater than 0";
    public static final String FUTURE = "Date has to be in the future";

    @NotBlank(message = MANDATORY)
    private String name;

    @NotBlank(message = MANDATORY)
    private String organizer;

    private String description;

    @NotBlank(message = MANDATORY)
    private String location;

    @NotNull(message = MANDATORY)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // <input type="date"> sends YYYY-MM-DD
    private LocalDate date;

    @NotNull(message = MANDATORY)
    @DateTimeFormat(pattern = "HH:mm") // <input type="time"> sends something like 14:25
    private LocalTime time;

    @Positive(message = POSITIVE)
    @NotNull(message = MANDATORY)
    private Float price;

    @Positive(message = POSITIVE)
    @NotNull(message = MANDATORY)
    private Integer capacity;

    @NotBlank(message = MANDATORY)
    private String eventCategory;

    // Combined from date + time, returns null if one is missing, since it is already handled by @NotNull on date/time
    @Future(message = FUTURE)
    public LocalDateTime getDateTime() {
        return (date == null || time == null) ? null : LocalDateTime.of(date, time);    }

    public EventForm setName(String name) {
        this.name = name;
        return this;
    }

    public EventForm setOrganizer(String organizer) {
        this.organizer = organizer;
        return this;
    }

    public EventForm setDescription(String description) {
        this.description = description;
        return this;
    }

    public EventForm setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public EventForm setTime(LocalTime time) {
        this.time = time;
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
