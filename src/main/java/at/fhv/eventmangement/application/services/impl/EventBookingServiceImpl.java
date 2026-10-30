package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.EventmanagementApplicationRunner;
import at.fhv.eventmangement.application.dto.EventBookingDto;
import at.fhv.eventmangement.application.form.EventBookingForm;
import at.fhv.eventmangement.application.mappers.EventBookingMapper;
import at.fhv.eventmangement.application.services.EventBookingService;
import at.fhv.eventmangement.domain.exceptions.ValidationException;
import at.fhv.eventmangement.domain.model.*;
import at.fhv.eventmangement.domain.repositories.CustomerRepository;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import at.fhv.eventmangement.domain.repositories.EventDateRepository;
import at.fhv.eventmangement.infrastructure.BookingNumberGenerator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class EventBookingServiceImpl implements EventBookingService {

    private final EventDateRepository eventDateRepository;
    private final EventBookingRepository eventBookingRepository;
    private final CustomerRepository customerRepository;
    private final BookingNumberGenerator bookingNumberGenerator;
    private final EventBookingMapper eventBookingMapper;

    public EventBookingServiceImpl(EventDateRepository eventDateRepository, EventBookingRepository eventBookingRepository, CustomerRepository customerRepository, BookingNumberGenerator bookingNumberGenerator, EventBookingMapper eventBookingMapper) {
        this.eventDateRepository = eventDateRepository;
        this.eventBookingRepository = eventBookingRepository;
        this.customerRepository = customerRepository;
        this.bookingNumberGenerator = bookingNumberGenerator;
        this.eventBookingMapper = eventBookingMapper;
    }

    @Override
    @Transactional
    public EventBookingDto book(UUID eventId, EventBookingForm form) {

        EventDate eventDate = eventDateRepository.findById(form.getEventDateId())
                .filter(date -> date.getEvent().getId().equals(eventId))
                .orElseThrow(() -> new ValidationException(Map.of("eventDateId", "unknown event date")));

        Event event = eventDate.getEvent();

        if (event.getEventStatus() != EventStatus.ACTIVE) {
            throw new ValidationException(Map.of("eventDateId", "event is not open for booking"));
        }

        // Only group bookings choose the number of places
        int places = form.getEventBookingType() == EventBookingType.GROUP ? form.getPlaces() : 1;

        int booked = eventBookingRepository.sumBookedPlaces(eventDate.getId());
        if (booked + places > event.getMaxParticipants()) {
            throw new ValidationException(Map.of("places",
                    "only " + (event.getMaxParticipants() - booked) + " places left"));
        }

        EventBooking booking = EventBooking.builder()
                .bookingNumber(bookingNumberGenerator.next())
                .eventDate(eventDate)
                .customer(currentCustomer())
                .eventBookingType(form.getEventBookingType())
                .places(places)
                .price(event.getPrice().multiply(BigDecimal.valueOf(places)))
                .paymentMethod(form.getPaymentMethod())
                .eventBookingStatus(EventBookingStatus.CONFIRMED)
                .build();

        return eventBookingMapper.toDto(eventBookingRepository.save(booking));
    }

    // Only until there is a login
    //every booking belongs to the default customer!!
    private Customer currentCustomer() {
        return customerRepository.findByContactInfoName(EventmanagementApplicationRunner.DEFAULT_CUSTOMER).orElseThrow();
    }
}
