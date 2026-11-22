package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.form.EventBookingForm;
import at.fhv.eventmangement.application.mappers.EventBookingMapper;
import at.fhv.eventmangement.application.services.BookingNumberGenerator;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.exceptions.NotEnoughPlacesException;
import at.fhv.eventmangement.domain.model.*;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import at.fhv.eventmangement.domain.repositories.EventDateRepository;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Bookings belong to the customer linked to the logged-in account
@ExtendWith(MockitoExtension.class)
class EventBookingServiceImplTest {

    private static final String USERNAME = "anna";

    @Mock
    private EventDateRepository eventDateRepository;
    @Mock
    private EventBookingRepository eventBookingRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private BookingNumberGenerator bookingNumberGenerator;
    @Mock
    private EventBookingMapper eventBookingMapper;

    @InjectMocks
    private EventBookingServiceImpl eventBookingService;

    private final UUID eventId = UUID.randomUUID();
    private final Customer anna = Customer.builder()
            .id(UUID.randomUUID())
            .contactInfo(new ContactInfo("Anna", "Street 1", "anna@test.com"))
            .build();
    private EventDate eventDate;

    @BeforeEach
    void setUp() {
        Event event = Event.builder()
                .id(eventId)
                .name("Hike")
                .price(new BigDecimal("20.00"))
                .maxParticipants(10)
                .eventStatus(EventStatus.ACTIVE)
                .build();
        eventDate = EventDate.builder().id(UUID.randomUUID()).event(event).build();
    }

    private EventBookingForm individualBooking() {
        return EventBookingForm.builder()
                .eventDateId(eventDate.getId())
                .eventBookingType(EventBookingType.INDIVIDUAL)
                .paymentMethod(PaymentMethod.CARD)
                .build();
    }

    private void loggedIn(Customer linkedCustomer) {
        when(userAccountRepository.findByUsername(USERNAME)).thenReturn(Optional.of(UserAccount.builder()
                .username(USERNAME)
                .enabled(true)
                .customer(linkedCustomer)
                .build()));
    }

    @Test
    void book_belongsToCustomerOfLoggedInAccount() throws Exception {
        when(eventDateRepository.findById(eventDate.getId())).thenReturn(Optional.of(eventDate));
        when(eventBookingRepository.sumBookedPlaces(eventDate.getId())).thenReturn(0);
        when(bookingNumberGenerator.next()).thenReturn("BK-TEST1234");
        loggedIn(anna);
        when(eventBookingRepository.save(any(EventBooking.class))).thenAnswer(inv -> inv.getArgument(0));

        eventBookingService.book(eventId, individualBooking(), USERNAME);

        ArgumentCaptor<EventBooking> captor = ArgumentCaptor.forClass(EventBooking.class);
        verify(eventBookingRepository).save(captor.capture());
        EventBooking saved = captor.getValue();
        assertThat(saved.getCustomer()).isSameAs(anna);
        assertThat(saved.getBookingNumber()).isEqualTo("BK-TEST1234");
        assertThat(saved.getPlaces()).isEqualTo(1);
        assertThat(saved.getPrice()).isEqualByComparingTo("20.00");
        assertThat(saved.getEventBookingStatus()).isEqualTo(EventBookingStatus.CONFIRMED);
    }

    @Test
    void book_accountWithoutCustomer_isRejected() {
        when(eventDateRepository.findById(eventDate.getId())).thenReturn(Optional.of(eventDate));
        when(eventBookingRepository.sumBookedPlaces(eventDate.getId())).thenReturn(0);
        when(bookingNumberGenerator.next()).thenReturn("BK-TEST1234");
        loggedIn(null);

        assertThatThrownBy(() -> eventBookingService.book(eventId, individualBooking(), USERNAME))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Your account is not linked to a customer");
        verify(eventBookingRepository, never()).save(any());
    }

    @Test
    void book_fullyBooked_isRejectedBeforeTheAccountIsLoaded() {
        when(eventDateRepository.findById(eventDate.getId())).thenReturn(Optional.of(eventDate));
        when(eventBookingRepository.sumBookedPlaces(eventDate.getId())).thenReturn(10);

        assertThatThrownBy(() -> eventBookingService.book(eventId, individualBooking(), USERNAME))
                .isInstanceOf(NotEnoughPlacesException.class);
        verify(userAccountRepository, never()).findByUsername(any());
        verify(eventBookingRepository, never()).save(any());
    }
}