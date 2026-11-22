package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.form.EventBookingForm;
import at.fhv.eventmangement.application.mappers.EventBookingMapper;
import at.fhv.eventmangement.application.services.BookingNumberGenerator;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.application.services.NotificationService;
import at.fhv.eventmangement.domain.exceptions.NotEnoughPlacesException;
import at.fhv.eventmangement.domain.model.*;
import at.fhv.eventmangement.domain.repositories.CustomerRepository;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import at.fhv.eventmangement.domain.repositories.EventDateRepository;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import at.fhv.eventmangement.domain.repositories.InvoiceRepository;
import at.fhv.eventmangement.application.dto.EventBookingDto;
import java.util.List;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;import org.junit.jupiter.api.BeforeEach;
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
    private final Customer anna = Customer.builder()
            .id(UUID.randomUUID())
            .contactInfo(new ContactInfo("Anna", "Street 1", "anna@test.com"))
            .build();
    private EventDate eventDate;
    private final UUID dateId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        Event event = Event.builder()
                .id(eventId)
                .name("Summer Hike")
                .price(new BigDecimal("150.00"))
                .maxParticipants(10)
                .eventStatus(EventStatus.ACTIVE).build();
        EventDate date = EventDate.builder()
                .id(dateId).event(event)
                .startTime(LocalDateTime.of(2027, 5, 1, 9, 0))
                .endTime(LocalDateTime.of(2027, 5, 1, 17, 0))
                .build();
        when(eventDateRepository.findById(dateId)).thenReturn(Optional.of(date));
        Event event = Event.builder().id(eventId).name("Summer Hike").price(new BigDecimal("150.00"))
                .maxParticipants(10).eventStatus(EventStatus.ACTIVE).build();
        EventDate date = EventDate.builder().id(dateId).event(event)
                .startTime(LocalDateTime.of(2027, 5, 1, 9, 0)).endTime(LocalDateTime.of(2027, 5, 1, 17, 0)).build();
        lenient().when(eventDateRepository.findById(dateId)).thenReturn(Optional.of(date)); //lenient: only booking tests use the event date
    }

    private EventBookingForm individualBooking() {
        return EventBookingForm.builder()
                .eventDateId(eventDate.getId())
                .eventBookingType(EventBookingType.INDIVIDUAL)
                .paymentMethod(PaymentMethod.CARD)
                .build();
        }
    private EventBookingForm groupForm(int places) {
        return EventBookingForm.builder().eventDateId(dateId).eventBookingType(EventBookingType.GROUP)
                .places(places).paymentMethod(PaymentMethod.CARD).build();
    private void loggedIn(Customer linkedCustomer) {
        when(userAccountRepository.findByUsername(USERNAME)).thenReturn(Optional.of(UserAccount.builder()
                .username(USERNAME)
                .enabled(true)
                .customer(linkedCustomer)
                .build()));
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
    void book_belongsToCustomerOfLoggedInAccount() throws Exception {
        when(eventDateRepository.findById(eventDate.getId())).thenReturn(Optional.of(eventDate));
        when(eventBookingRepository.sumBookedPlaces(eventDate.getId())).thenReturn(0);
        when(bookingNumberGenerator.next()).thenReturn("BK-TEST1234");
        loggedIn(anna);
        when(eventBookingRepository.save(any(EventBooking.class))).thenAnswer(inv -> inv.getArgument(0));

        eventBookingService.book(eventId, groupForm(2));
        eventBookingService.book(eventId, individualBooking(), USERNAME);

        ArgumentCaptor<Invoice> invoice = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoice.capture());
        assertThat(invoice.getValue().getBookingId()).isEqualTo(bookingId);
        assertThat(invoice.getValue().getInvoiceStatus()).isEqualTo(InvoiceStatus.INTERM);
        assertThat(invoice.getValue().getContactInfo().getName()).isEqualTo("Anna");
        assertThat(invoice.getValue().getItems()).singleElement()
                .satisfies(item -> assertThat(item.getQuantity()).isEqualTo(2));
        assertThat(invoice.getValue().getGrossTotal()).isEqualByComparingTo("360.00");
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
    void book_notEnoughPlaces_createsNoInvoice() {
        when(eventBookingRepository.sumBookedPlaces(dateId)).thenReturn(9);
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

        assertThatThrownBy(() -> eventBookingService.book(eventId, groupForm(2)))
    @Test
    void book_fullyBooked_isRejectedBeforeTheAccountIsLoaded() {
        when(eventDateRepository.findById(eventDate.getId())).thenReturn(Optional.of(eventDate));
        when(eventBookingRepository.sumBookedPlaces(eventDate.getId())).thenReturn(10);

        assertThatThrownBy(() -> eventBookingService.book(eventId, individualBooking(), USERNAME))
                .isInstanceOf(NotEnoughPlacesException.class);
        verify(notificationService, never()).notifyBookingConfirmed(any(), any());
        verify(invoiceRepository, never()).save(any());
        verify(userAccountRepository, never()).findByUsername(any());
        verify(eventBookingRepository, never()).save(any());
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

    private EventBooking bookingOn(LocalDateTime start, EventBookingStatus status) {
        return EventBooking.builder()
                .bookingNumber("BK-" + start.getDayOfMonth() + status)
                .eventDate(EventDate.builder().startTime(start).endTime(start.plusHours(3)).build())
                .eventBookingStatus(status)
                .build();
    }

    @Test
    void getNextBooking_returnsSoonestUpcomingBookingThatIsNotCancelled() throws Exception {
        Customer anna = Customer.builder().id(UUID.randomUUID())
                .contactInfo(new ContactInfo("Anna", "Street 1", "anna@example.com")).build();
        LocalDateTime now = LocalDateTime.now();
        EventBooking past = bookingOn(now.minusDays(3), EventBookingStatus.CONFIRMED);
        EventBooking cancelledSooner = bookingOn(now.plusDays(2), EventBookingStatus.CANCELLED);
        EventBooking next = bookingOn(now.plusDays(10), EventBookingStatus.CONFIRMED);
        EventBooking later = bookingOn(now.plusDays(20), EventBookingStatus.OPTION);
        EventBookingDto nextDto = mock(EventBookingDto.class);
        when(customerRepository.findByContactInfoName(any())).thenReturn(Optional.of(anna));
        when(eventBookingRepository.findByCustomerId(anna.getId()))
                .thenReturn(List.of(later, past, cancelledSooner, next));
        when(eventBookingMapper.toDto(next)).thenReturn(nextDto);

        assertThat(eventBookingService.getNextBookingForCurrentCustomer()).contains(nextDto);
    }

    @Test
    void getNextBooking_onlyPastOrCancelledBookings_isEmpty() throws Exception {
        Customer anna = Customer.builder().id(UUID.randomUUID())
                .contactInfo(new ContactInfo("Anna", "Street 1", "anna@example.com")).build();
        LocalDateTime now = LocalDateTime.now();
        when(customerRepository.findByContactInfoName(any())).thenReturn(Optional.of(anna));
        when(eventBookingRepository.findByCustomerId(anna.getId())).thenReturn(List.of(
                bookingOn(now.minusDays(1), EventBookingStatus.CONFIRMED),
                bookingOn(now.plusDays(5), EventBookingStatus.EXPIRED)));

        assertThat(eventBookingService.getNextBookingForCurrentCustomer()).isEmpty();
    }
}