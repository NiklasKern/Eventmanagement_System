package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.dto.EventBookingDto;
import at.fhv.eventmangement.application.form.EventBookingForm;
import at.fhv.eventmangement.application.mappers.EventBookingMapper;
import at.fhv.eventmangement.application.services.BookingNumberGenerator;
import at.fhv.eventmangement.application.services.NotificationService;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.exceptions.NotEnoughPlacesException;
import at.fhv.eventmangement.domain.model.*;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import at.fhv.eventmangement.domain.repositories.EventDateRepository;
import at.fhv.eventmangement.domain.repositories.InvoiceRepository;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
    private InvoiceRepository invoiceRepository;
    @Mock
    private BookingNumberGenerator bookingNumberGenerator;
    @Mock
    private EventBookingMapper eventBookingMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EventBookingServiceImpl eventBookingService;

    private final UUID eventId = UUID.randomUUID();
    private final UUID dateId = UUID.randomUUID();
    private final Customer anna = Customer.builder()
            .id(UUID.randomUUID())
            .contactInfo(new ContactInfo("Anna", "Street 1", "anna@test.com"))
            .build();
    private EventDate eventDate;

    @BeforeEach
    void setUp() {
        Event event = Event.builder()
                .id(eventId)
                .name("Summer Hike")
                .price(new BigDecimal("150.00"))
                .maxParticipants(10)
                .eventStatus(EventStatus.ACTIVE)
                .build();
        eventDate = EventDate.builder()
                .id(dateId)
                .event(event)
                .startTime(LocalDateTime.of(2027, 5, 1, 9, 0))
                .endTime(LocalDateTime.of(2027, 5, 1, 17, 0))
                .build();
    }

    private EventBookingForm groupBooking(int places) {
        return EventBookingForm.builder()
                .eventDateId(dateId)
                .eventBookingType(EventBookingType.GROUP)
                .places(places)
                .paymentMethod(PaymentMethod.CARD)
                .build();
    }

    // The account of the logged-in user "anna", linked to the given customer (or none)
    private void loggedIn(Customer linkedCustomer) {
        when(userAccountRepository.findByUsername(USERNAME)).thenReturn(Optional.of(UserAccount.builder()
                .username(USERNAME)
                .enabled(true)
                .customer(linkedCustomer)
                .build()));
    }

    // Booking succeeds up to the point where it is saved (save returns the booking with an id)
    private UUID bookingCanBeSaved() {
        UUID bookingId = UUID.randomUUID();
        when(eventDateRepository.findById(dateId)).thenReturn(Optional.of(eventDate));
        when(eventBookingRepository.sumBookedPlaces(dateId)).thenReturn(0);
        when(bookingNumberGenerator.next()).thenReturn("BK-ABCDEFGH");
        when(eventBookingRepository.save(any(EventBooking.class))).thenAnswer(inv -> {
            EventBooking booking = inv.getArgument(0);
            booking.setId(bookingId);
            return booking;
        });
        return bookingId;
    }

    // book

    @Test
    void book_belongsToCustomerOfLoggedInAccount() throws Exception {
        bookingCanBeSaved();
        loggedIn(anna);

        eventBookingService.book(eventId, groupBooking(2), USERNAME);

        ArgumentCaptor<EventBooking> captor = ArgumentCaptor.forClass(EventBooking.class);
        verify(eventBookingRepository).save(captor.capture());
        EventBooking saved = captor.getValue();
        assertThat(saved.getCustomer()).isSameAs(anna);
        assertThat(saved.getBookingNumber()).isEqualTo("BK-ABCDEFGH");
        assertThat(saved.getPlaces()).isEqualTo(2);
        assertThat(saved.getPrice()).isEqualByComparingTo("300.00");
        assertThat(saved.getEventBookingStatus()).isEqualTo(EventBookingStatus.CONFIRMED);
    }

    @Test
    void book_createsInterimInvoiceForSavedBooking() throws Exception {
        UUID bookingId = bookingCanBeSaved();
        loggedIn(anna);

        eventBookingService.book(eventId, groupBooking(2), USERNAME);

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
    void book_sendsBookingConfirmationToCustomer() throws Exception {
        bookingCanBeSaved();
        loggedIn(anna);

        eventBookingService.book(eventId, groupBooking(2), USERNAME);

        verify(notificationService).notifyBookingConfirmed(any(Customer.class), any(EventBooking.class));
    }

    @Test
    void book_accountWithoutCustomer_isRejected() {
        when(eventDateRepository.findById(dateId)).thenReturn(Optional.of(eventDate));
        when(eventBookingRepository.sumBookedPlaces(dateId)).thenReturn(0);
        when(bookingNumberGenerator.next()).thenReturn("BK-ABCDEFGH");
        loggedIn(null);

        assertThatThrownBy(() -> eventBookingService.book(eventId, groupBooking(2), USERNAME))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Your account is not linked to a customer");
        verify(eventBookingRepository, never()).save(any());
        verify(invoiceRepository, never()).save(any());
        verify(notificationService, never()).notifyBookingConfirmed(any(), any());
    }

    @Test
    void book_notEnoughPlaces_savesNothingAndSendsNothing() {
        when(eventDateRepository.findById(dateId)).thenReturn(Optional.of(eventDate));
        when(eventBookingRepository.sumBookedPlaces(dateId)).thenReturn(9);

        assertThatThrownBy(() -> eventBookingService.book(eventId, groupBooking(2), USERNAME))
                .isInstanceOf(NotEnoughPlacesException.class);
        verify(userAccountRepository, never()).findByUsername(any());
        verify(eventBookingRepository, never()).save(any());
        verify(invoiceRepository, never()).save(any());
        verify(notificationService, never()).notifyBookingConfirmed(any(), any());
    }

    // own bookings

    @Test
    void getBookingsForCurrentCustomer_returnsBookingsOfLinkedCustomer() throws Exception {
        EventBooking booking = EventBooking.builder().bookingNumber("BK-ABCDEFGH").customer(anna).build();
        EventBookingDto dto = mock(EventBookingDto.class);
        loggedIn(anna);
        when(eventBookingRepository.findByCustomerId(anna.getId())).thenReturn(List.of(booking));
        when(eventBookingMapper.toDto(booking)).thenReturn(dto);

        assertThat(eventBookingService.getBookingsForCurrentCustomer(USERNAME)).containsExactly(dto);
    }

    @Test
    void getBookingByNumber_ownBooking_isFound() throws Exception {
        EventBooking booking = EventBooking.builder().bookingNumber("BK-ABCDEFGH").customer(anna).build();
        EventBookingDto dto = mock(EventBookingDto.class);
        loggedIn(anna);
        when(eventBookingRepository.findByBookingNumber("BK-ABCDEFGH")).thenReturn(Optional.of(booking));
        when(eventBookingMapper.toDto(booking)).thenReturn(dto);

        assertThat(eventBookingService.getBookingByNumber("BK-ABCDEFGH", USERNAME)).contains(dto);
    }

    @Test
    void getBookingByNumber_bookingOfAnotherCustomer_isNotFound() throws Exception {
        Customer bernd = Customer.builder().id(UUID.randomUUID()).build();
        EventBooking foreign = EventBooking.builder().bookingNumber("BK-OTHER123").customer(bernd).build();
        loggedIn(anna);
        when(eventBookingRepository.findByBookingNumber("BK-OTHER123")).thenReturn(Optional.of(foreign));

        assertThat(eventBookingService.getBookingByNumber("BK-OTHER123", USERNAME)).isEmpty();
        verify(eventBookingMapper, never()).toDto(any(EventBooking.class));
    }

    // next booking for the main page

    private EventBooking bookingOn(LocalDateTime start, EventBookingStatus status) {
        return EventBooking.builder()
                .bookingNumber("BK-" + start.getDayOfMonth() + status)
                .customer(anna)
                .eventDate(EventDate.builder().startTime(start).endTime(start.plusHours(3)).build())
                .eventBookingStatus(status)
                .build();
    }

    @Test
    void getNextBooking_returnsSoonestUpcomingBookingThatIsNotCancelled() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        EventBooking past = bookingOn(now.minusDays(3), EventBookingStatus.CONFIRMED);
        EventBooking cancelledSooner = bookingOn(now.plusDays(2), EventBookingStatus.CANCELLED);
        EventBooking next = bookingOn(now.plusDays(10), EventBookingStatus.CONFIRMED);
        EventBooking later = bookingOn(now.plusDays(20), EventBookingStatus.OPTION);
        EventBookingDto nextDto = mock(EventBookingDto.class);
        loggedIn(anna);
        when(eventBookingRepository.findByCustomerId(anna.getId()))
                .thenReturn(List.of(later, past, cancelledSooner, next));
        when(eventBookingMapper.toDto(next)).thenReturn(nextDto);

        assertThat(eventBookingService.getNextBookingForCurrentCustomer(USERNAME)).contains(nextDto);
    }

    @Test
    void getNextBooking_onlyPastOrExpiredBookings_isEmpty() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        loggedIn(anna);
        when(eventBookingRepository.findByCustomerId(anna.getId())).thenReturn(List.of(
                bookingOn(now.minusDays(1), EventBookingStatus.CONFIRMED),
                bookingOn(now.plusDays(5), EventBookingStatus.EXPIRED)));

        assertThat(eventBookingService.getNextBookingForCurrentCustomer(USERNAME)).isEmpty();
    }

    @Test
    void getNextBooking_accountWithoutCustomer_isRejected() {
        loggedIn(null);

        assertThatThrownBy(() -> eventBookingService.getNextBookingForCurrentCustomer(USERNAME))
                .isInstanceOf(CustomerNotFoundException.class);
    }
}