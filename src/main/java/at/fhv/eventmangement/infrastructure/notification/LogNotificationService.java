package at.fhv.eventmangement.infrastructure.notification;

import at.fhv.eventmangement.application.services.NotificationService;
import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// Placeholder for the email service to inform customers about changes to the event
@Slf4j
@Service
public class LogNotificationService implements NotificationService {
    @Override
    public void notifyEventChanged(Customer customer, Event event) {
        log.info("Notification to {} <{}>: the event '{}' you booked has been changed.",
                customer.getContactInfo().getName(), customer.getContactInfo().getEmail(), event.getName());
    }
}
