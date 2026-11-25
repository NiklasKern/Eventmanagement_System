package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.dto.EventBookingDto;
import at.fhv.eventmangement.application.mappers.EventBookingMapper;
import at.fhv.eventmangement.application.services.BookingNumberGenerator;
import at.fhv.eventmangement.application.services.EventBookingService;
import at.fhv.eventmangement.application.services.NotificationService;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventDateNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventNotBookableException;
import at.fhv.eventmangement.domain.exceptions.NotEnoughPlacesException;
import at.fhv.eventmangement.domain.model.*;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import at.fhv.eventmangement.domain.repositories.EventDateRepository;
import at.fhv.eventmangement.domain.repositories.InvoiceRepository;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EventBookingServiceImpl implements EventBookingService {

    private final EventDateRepository eventDateRepository;
    private final EventBookingRepository eventBookingRepository;
    private final UserAccountRepository userAccountRepository;
    private final InvoiceRepository invoiceRepository;
    private final BookingNumberGenerator bookingNumberGenerator;
    private final EventBookingMapper eventBookingMapper;
    private final NotificationService notificationService;

    public EventBookingServiceImpl(EventDateRepository eventDateRepository,
                                   EventBookingRepository eventBookingRepository,
                                   UserAccountRepository userAccountRepository,
                                   InvoiceRepository invoiceRepository,
                                   BookingNumberGenerator bookingNumberGenerator,
                                   EventBookingMapper eventBookingMapper,
                                   NotificationService notificationService,
                                   InvoiceRepository invoiceRepository) {
        this.eventDateRepository = eventDateRepository;
        this.eventBookingRepository = eventBookingRepository;
        this.userAccountRepository = userAccountRepository;
        this.invoiceRepository = invoiceRepository;
        this.bookingNumberGenerator = bookingNumberGenerator;
        this.eventBookingMapper = eventBookingMapper;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventBookingDto book(UUID eventId, EventBookingForm form, String username)
            throws EventDateNotFoundException, EventNotBookableException, NotEnoughPlacesException, CustomerNotFoundException {

        EventDate eventDate = eventDateRepository.findById(form.getEventDateId())
                .filter(date -> date.getEvent().getId().equals(eventId))
                .orElseThrow(EventDateNotFoundException::new);

        Event event = eventDate.getEvent();

        if (event.getEventStatus() != EventStatus.ACTIVE) {
            throw new EventNotBookableException();
        }

        // Only group bookings choose the number of places
        int places = form.getEventBookingType() == EventBookingType.GROUP ? form.getPlaces() : 1;

        int booked = eventBookingRepository.sumBookedPlaces(eventDate.getId());
        if (booked + places > event.getMaxParticipants()) {
            throw new NotEnoughPlacesException(event.getMaxParticipants() - booked);
        }

        EventBooking booking = EventBooking.builder()
                .bookingNumber(bookingNumberGenerator.next())
                .eventDate(eventDate)
                .customer(currentCustomer(username))
                .eventBookingType(form.getEventBookingType())
                .places(places)
                .price(event.getPrice().multiply(BigDecimal.valueOf(places)))
                .paymentMethod(form.getPaymentMethod())
                .eventBookingStatus(EventBookingStatus.CONFIRMED)
                .build();

        EventBooking saved = eventBookingRepository.save(booking);

        // Interim invoice for the check-out review
        invoiceRepository.save(Invoice.createInterimFor(booking).setBookingId(saved.getId()));

        // Booking confirmation email (a failed email does not cancel the booking)
        notificationService.notifyBookingConfirmed(saved.getCustomer(), saved);

        return eventBookingMapper.toDto(saved);
    }

    @Override
    public List<String> getBookingTypes() {
        return Arrays.stream(EventBookingType.values()).map(Enum::name).toList();
    }

    @Override
    public List<String> getPaymentMethods() {
        return Arrays.stream(PaymentMethod.values()).map(Enum::name).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventBookingDto> getBookingsForCurrentCustomer(String username) throws CustomerNotFoundException {
        Customer customer = currentCustomer(username);
        return eventBookingRepository.findByCustomerId(customer.getId()).stream()
                .map(eventBookingMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventBookingDto> getBookingByNumber(String bookingNumber, String username)
            throws CustomerNotFoundException {
        UUID customerId = currentCustomer(username).getId();
        return eventBookingRepository.findByBookingNumber(bookingNumber)
                // a booking of another customer is treated like a booking that does not exist
                .filter(booking -> booking.getCustomer() != null
                        && customerId.equals(booking.getCustomer().getId()))
                .map(eventBookingMapper::toDto);
    }

    // The booking belongs to the customer linked to the logged-in account
    private Customer currentCustomer(String username) throws CustomerNotFoundException {
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("No account for logged-in user " + username));
        if (account.getCustomer() == null) {
            throw new CustomerNotFoundException("Your account is not linked to a customer");
        }
        return account.getCustomer();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventBookingDto> getNextBookingForCurrentCustomer() throws CustomerNotFoundException {
        LocalDateTime now = LocalDateTime.now();
        return eventBookingRepository.findByCustomerId(currentCustomer().getId()).stream()
                .filter(booking -> booking.getEventBookingStatus() != EventBookingStatus.CANCELLED
                        && booking.getEventBookingStatus() != EventBookingStatus.EXPIRED)
                .filter(booking -> booking.getEventDate() != null && booking.getEventDate().getStartTime().isAfter(now))
                .min(Comparator.comparing(booking -> booking.getEventDate().getStartTime()))
                .map(eventBookingMapper::toDto);
    }
}