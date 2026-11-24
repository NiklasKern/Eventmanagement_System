package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.model.Event;
import at.fhv.eventmangement.domain.model.EventBooking;

public interface NotificationService {
    // Informs the customer that an event they booked has changed.
    // Returns true only if the notification was actually sent.
    boolean notifyEventChanged(Customer customer, Event event);
    boolean notifyBookingConfirmed(Customer customer, EventBooking booking);
    boolean notifyEventCancelled(Customer customer, Event event);
}
