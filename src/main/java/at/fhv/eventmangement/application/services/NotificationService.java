package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.model.Event;

// TODO: implement email notifications for this, right now it is only via log
public interface NotificationService {
    void notifyEventChanged(Customer customer, Event event);
}
