package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.form.EventBookingForm;
import at.fhv.eventmangement.application.mappers.EventBookingMapper;
import at.fhv.eventmangement.application.services.BookingNumberGenerator;
import at.fhv.eventmangement.application.services.NotificationService;
import at.fhv.eventmangement.domain.exceptions.NotEnoughPlacesException;
import at.fhv.eventmangement.domain.model.*;
import at.fhv.eventmangement.domain.repositories.CustomerRepository;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import at.fhv.eventmangement.domain.repositories.EventDateRepository;
import at.fhv.eventmangement.domain.repositories.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventBookingServiceImplTest {

    @Mock
    private EventDateRepository eventDateRepository;
    @Mock
    private EventBookingRepository eventBookingRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private BookingNumberGenerator bookingNumberGenerator;
    @Mock
    private EventBookingMapper eventBookingMapper;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EventBookingServiceImpl eventBookingService;

    private final UUID eventId = UUID.randomUUID();
    private final UUID dateId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        Event event = Event.builder().id(eventId).name("Summer Hike").price(new BigDecimal("150.00"))
                .maxParticipants(10).eventStatus(EventStatus.ACTIVE).build();
        EventDate date = EventDate.builder().id(dateId).event(event)
                .startTime(LocalDateTime.of(2027, 5, 1, 9, 0)).endTime(LocalDateTime.of(2027, 5, 1, 17, 0)).build();
        when(eventDateRepository.findById(dateId)).thenReturn(Optional.of(date));
    }

    private EventBookingForm groupForm(int places) {
        return EventBookingForm.builder().eventDateId(dateId).eventBookingType(EventBookingType.GROUP)
                .places(places).paymentMethod(PaymentMethod.CARD).build();
    }

    @Test
    void book_createsInterimInvoiceForSavedBooking() throws Exception {
        UUID bookingId = UUID.randomUUID();
        when(eventBookingRepository.sumBookedPlaces(dateId)).thenReturn(0);
        when(customerRepository.findByContactInfoName(any())).thenReturn(Optional.of(Customer.builder()
                .contactInfo(new ContactInfo("Anna", "Street 1", "anna@example.com")).build()));
        when(bookingNumberGenerator.next()).thenReturn("BK-ABCDEFGH");
        when(eventBookingRepository.save(any())).thenAnswer(inv -> {
            EventBooking b = inv.getArgument(0);
            b.setId(bookingId);
            return b;
        });

        eventBookingService.book(eventId, groupForm(2));

        ArgumentCaptor<Invoice> invoice = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoice.capture());
        assertThat(invoice.getValue().getBookingId()).isEqualTo(bookingId);
        assertThat(invoice.getValue().getInvoiceStatus()).isEqualTo(InvoiceStatus.INTERM);
        assertThat(invoice.getValue().getContactInfo().getName()).isEqualTo("Anna");
        assertThat(invoice.getValue().getItems()).singleElement()
                .satisfies(item -> assertThat(item.getQuantity()).isEqualTo(2));
        assertThat(invoice.getValue().getGrossTotal()).isEqualByComparingTo("360.00");
    }

    @Test
    void book_notEnoughPlaces_createsNoInvoice() {
        when(eventBookingRepository.sumBookedPlaces(dateId)).thenReturn(9);

        assertThatThrownBy(() -> eventBookingService.book(eventId, groupForm(2)))
                .isInstanceOf(NotEnoughPlacesException.class);
        verify(notificationService, never()).notifyBookingConfirmed(any(), any());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void book_sendsBookingConfirmationToCustomer() throws Exception {
        when(eventBookingRepository.sumBookedPlaces(dateId)).thenReturn(0);
        when(customerRepository.findByContactInfoName(any())).thenReturn(Optional.of(Customer.builder()
                .contactInfo(new ContactInfo("Anna", "Street 1", "anna@example.com")).build()));
        when(bookingNumberGenerator.next()).thenReturn("BK-ABCDEFGH");
        when(eventBookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        eventBookingService.book(eventId, groupForm(2));

        verify(notificationService).notifyBookingConfirmed(any(Customer.class), any(EventBooking.class));
    }
}