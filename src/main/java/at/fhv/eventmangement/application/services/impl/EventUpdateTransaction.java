package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.form.EventDateForm;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.domain.exceptions.EventAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.EventCategoryNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventDateHasBookingsException;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import at.fhv.eventmangement.domain.exceptions.MaxParticipantsBelowBookedPlacesException;
import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.model.Event;
import at.fhv.eventmangement.domain.model.EventCategory;
import at.fhv.eventmangement.domain.model.EventDate;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import at.fhv.eventmangement.domain.repositories.EventCategoryRepository;
import at.fhv.eventmangement.domain.repositories.EventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// Database part of an event update. Kept in its own bean so the transaction is committed
// before EventServiceImpl sends any notifications (no mails for rolled back changes).
@Component
class EventUpdateTransaction {

    record Result(Event event, List<Customer> customersToNotify) {
    }

    private final EventRepository eventRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final EventBookingRepository eventBookingRepository;

    EventUpdateTransaction(EventRepository eventRepository,
                           EventCategoryRepository eventCategoryRepository,
                           EventBookingRepository eventBookingRepository) {
        this.eventRepository = eventRepository;
        this.eventCategoryRepository = eventCategoryRepository;
        this.eventBookingRepository = eventBookingRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public Result apply(UUID id, EventForm form)
            throws EventNotFoundException, EventAlreadyExistsException, EventCategoryNotFoundException,
            EventDateHasBookingsException, MaxParticipantsBelowBookedPlacesException {

        Event event = eventRepository.findById(id).orElseThrow(EventNotFoundException::new);

        String name = form.getName().trim();
        if (!event.getName().equalsIgnoreCase(name) && eventRepository.existsByName(name)) {
            throw new EventAlreadyExistsException();
        }

        EventCategory category = eventCategoryRepository.findById(form.getEventCategoryId())
                .orElseThrow(EventCategoryNotFoundException::new);

        Map<UUID, EventDate> existing = event.getEventDates().stream()
                .collect(Collectors.toMap(EventDate::getId, Function.identity()));

        List<EventDate> updatedDates = new ArrayList<>();
        for (EventDateForm dateForm : form.getEventDates()) {
            EventDate date = dateForm.getId() == null ? null : existing.remove(dateForm.getId());
            if (date != null) {
                // Bookings are counted per date, so every kept date must still fit its booked places
                int booked = eventBookingRepository.sumBookedPlaces(date.getId());
                if (form.getMaxParticipants() < booked) {
                    throw new MaxParticipantsBelowBookedPlacesException(booked);
                }
                date.setStartTime(dateForm.getStartTime());
                date.setEndTime(dateForm.getEndTime());
                updatedDates.add(date);
            } else {
                updatedDates.add(EventDate.builder()
                        .event(event)
                        .startTime(dateForm.getStartTime())
                        .endTime(dateForm.getEndTime())
                        .build());
            }
        }

        // Dates left in "existing" were removed in the form
        for (EventDate removed : existing.values()) {
            if (eventBookingRepository.hasActiveBookings(removed.getId())) {
                throw new EventDateHasBookingsException();
            }
        }

        event.setName(name);
        event.setDescription(Optional.ofNullable(form.getDescription()).orElse(""));
        event.setLocation(form.getLocation());
        event.setPrice(form.getPrice());
        event.setMinParticipants(form.getMinParticipants());
        event.setMaxParticipants(form.getMaxParticipants());
        event.setEventCategory(category);
        event.getEventDates().clear();
        event.getEventDates().addAll(updatedDates);

        Event saved = eventRepository.save(event);

        // Every customer with an active booking, each only once
        Map<UUID, Customer> customers = new LinkedHashMap<>();
        eventBookingRepository.findActiveBookingsByEventId(id)
                .forEach(b -> customers.putIfAbsent(b.getCustomer().getId(), b.getCustomer()));

        return new Result(saved, List.copyOf(customers.values()));
    }
}
