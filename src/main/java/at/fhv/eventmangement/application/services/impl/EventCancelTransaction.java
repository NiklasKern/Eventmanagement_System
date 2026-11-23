package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.domain.exceptions.EventAlreadyCancelledException;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.model.Event;
import at.fhv.eventmangement.domain.model.EventBooking;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import at.fhv.eventmangement.domain.repositories.EventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
class EventCancelTransaction {

    record Result(Event event, List<Customer> customersToNotify) {
    }

    private final EventRepository eventRepository;
    private final EventBookingRepository eventBookingRepository;

    EventCancelTransaction(EventRepository eventRepository,
                           EventBookingRepository eventBookingRepository) {
        this.eventRepository = eventRepository;
        this.eventBookingRepository = eventBookingRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public Result apply(UUID id) throws EventNotFoundException, EventAlreadyCancelledException {
        Event event = eventRepository.findById(id).orElseThrow(EventNotFoundException::new);

        event.cancel();
        Event savedEvent = eventRepository.save(event);

        Map<UUID, Customer> customers = new LinkedHashMap<>();
        List<EventBooking> activeBookings = eventBookingRepository.findActiveBookingsByEventId(id);

        for (EventBooking booking : activeBookings) {
            booking.cancel();
            eventBookingRepository.save(booking);
            if (booking.getCustomer() != null) {
                customers.putIfAbsent(booking.getCustomer().getId(), booking.getCustomer());
            }
        }

        return new Result(savedEvent, List.copyOf(customers.values()));
    }
}