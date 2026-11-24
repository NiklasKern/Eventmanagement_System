package at.fhv.eventmangement.infrastructure.notification;

import at.fhv.eventmangement.application.services.NotificationService;
import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import at.fhv.eventmangement.domain.model.EventBooking;

// Sends notifications to customers via email (SMTP)
@Slf4j
@Service
@RequiredArgsConstructor
public class MailNotificationService implements NotificationService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:noreply@eventmanagement.at}")
    private String senderEmail;

    @Override
    public boolean notifyEventChanged(Customer customer, Event event) {
        if (customer == null || customer.getContactInfo() == null) {
            return false;
        }

        String customerName = customer.getContactInfo().getName();
        String customerEmail = customer.getContactInfo().getEmail();
        if (customerEmail == null || customerEmail.isBlank()) {
            log.warn("Customer {} has no email address, event change notification not sent", customerName);
            return false;
        }

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
            log.info("Event change notification sent to {} <{}>", customerName, customerEmail);
            return true;
        } catch (MailException e) {
            log.warn("Could not send event change notification to {}: {}", customerEmail, e.getMessage());
            return false;
        }
    }
    @Override
    public boolean notifyBookingConfirmed(Customer customer, EventBooking booking) {
        if (customer == null || customer.getContactInfo() == null || booking == null) {
            return false;
        }

        String customerName = customer.getContactInfo().getName();
        String customerEmail = customer.getContactInfo().getEmail();
        if (customerEmail == null || customerEmail.isBlank()) {
            log.warn("Customer {} has no email address, booking confirmation not sent", customerName);
            return false;
        }

        Event event = booking.getEventDate().getEvent();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(customerEmail);
            message.setSubject("Booking Confirmation: " + event.getName() + " (" + booking.getBookingNumber() + ")");
            message.setText(String.format(
                    "Hello %s,\n\nYour booking has been confirmed!\n\n" +
                            "Booking Number: %s\n" +
                            "Event: %s\n" +
                            "Location: %s\n" +
                            "Start Time: %s\n" +
                            "Places: %d\n" +
                            "Total Price: %s EUR\n\n" +
                            "You can view your booking details anytime in your account.",
                    customerName,
                    booking.getBookingNumber(),
                    event.getName(),
                    event.getLocation(),
                    booking.getEventDate().getStartTime(),
                    booking.getPlaces(),
                    booking.getPrice()
            ));

            javaMailSender.send(message);
            log.info("Booking confirmation sent to {} <{}> for booking {}",
                    customerName, customerEmail, booking.getBookingNumber());
            return true;
        } catch (MailException e) {
            log.warn("Could not send booking confirmation to {}: {}", customerEmail, e.getMessage());
            return false;
        }
    }
    @Override
    public boolean notifyEventCancelled(Customer customer, Event event) {
        if (customer == null || customer.getContactInfo() == null || event == null) {
            return false;
        }

        String customerName = customer.getContactInfo().getName();
        String customerEmail = customer.getContactInfo().getEmail();
        if (customerEmail == null || customerEmail.isBlank()) {
            log.warn("Customer {} has no email address, event cancellation notification not sent", customerName);
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(customerEmail);
            message.setSubject("Cancellation: Event '" + event.getName() + "' has been cancelled");
            message.setText(String.format(
                    "Hello %s,\n\nWe regret to inform you that the event '%s' (Location: %s) you booked has been cancelled.\n\nYour booking has been cancelled accordingly.",
                    customerName, event.getName(), event.getLocation()
            ));

            javaMailSender.send(message);
            log.info("Event cancellation notification sent to {} <{}>", customerName, customerEmail);
            return true;
        } catch (MailException e) {
            log.warn("Could not send event cancellation notification to {}: {}", customerEmail, e.getMessage());
            return false;
        }
    }
}
