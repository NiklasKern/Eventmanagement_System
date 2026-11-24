package at.fhv.eventmangement.infrastructure.notification;

import at.fhv.eventmangement.domain.model.ContactInfo;
import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.model.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailNotificationServiceCancelTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private MailNotificationService mailNotificationService;

    private static Customer customerWithEmail(String email) {
        return Customer.builder()
                .id(UUID.randomUUID())
                .contactInfo(new ContactInfo("Anna", "Street 1", email))
                .build();
    }

    @Test
    void notifyEventCancelled_validEmail_sendsMailAndReturnsTrue() {
        Customer customer = customerWithEmail("anna@test.com");
        Event event = Event.builder().name("Summer Hike").location("Dornbirn").build();

        boolean result = mailNotificationService.notifyEventCancelled(customer, event);

        assertThat(result).isTrue();
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void notifyEventCancelled_blankEmail_doesNotSendAndReturnsFalse() {
        Customer customer = customerWithEmail(" ");
        Event event = Event.builder().name("Summer Hike").location("Dornbirn").build();

        boolean result = mailNotificationService.notifyEventCancelled(customer, event);

        assertThat(result).isFalse();
        verifyNoInteractions(javaMailSender);
    }

    @Test
    void notifyEventCancelled_mailServerError_handlesExceptionAndReturnsFalse() {
        Customer customer = customerWithEmail("anna@test.com");
        Event event = Event.builder().name("Summer Hike").location("Dornbirn").build();
        doThrow(new MailSendException("SMTP error")).when(javaMailSender).send(any(SimpleMailMessage.class));

        boolean result = mailNotificationService.notifyEventCancelled(customer, event);

        assertThat(result).isFalse();
    }
}