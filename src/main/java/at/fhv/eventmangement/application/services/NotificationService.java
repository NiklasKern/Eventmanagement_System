package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.model.Event;

public interface NotificationService {
    // Informs the customer that an event they booked has changed.
    // Returns true only if the notification was actually sent.
    boolean notifyEventChanged(Customer customer, Event event);
}
