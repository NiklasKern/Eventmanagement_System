package at.fhv.eventmangement.infrastructure.notification;

import at.fhv.eventmangement.application.services.NotificationService;
import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogNotificationService implements NotificationService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:noreply@eventmanagement.at}")
    private String senderEmail;

    @Override
    public void notifyEventChanged(Customer customer, Event event) {
        if (customer == null || customer.getContactInfo() == null) {
            return;
        }

        String customerName = customer.getContactInfo().getName();
        String customerEmail = customer.getContactInfo().getEmail();

        log.info("Notification to {} <{}>: the event '{}' you booked has been changed.",
                customerName, customerEmail, event.getName());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(customerEmail);
            message.setSubject("Update: Event '" + event.getName() + "' has been modified");
            message.setText(String.format(
                    "Hello %s,\n\nThe event '%s' you booked has been updated.\nLocation: %s\n\nPlease check the new details in the system.",
                    customerName, event.getName(), event.getLocation()
            ));

            javaMailSender.send(message);
            log.info("Email sent successfully to {}", customerEmail);
        } catch (Exception e) {
            log.warn("Could not send email via SMTP to {}: {}", customerEmail, e.getMessage());
        }
    }
}