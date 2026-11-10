package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.EventmanagementApplicationRunner;
import at.fhv.eventmangement.application.dto.EventBookingDto;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventDateNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventNotBookableException;
import at.fhv.eventmangement.domain.exceptions.NotEnoughPlacesException;
import at.fhv.eventmangement.application.form.EventBookingForm;
import at.fhv.eventmangement.application.mappers.EventBookingMapper;
import at.fhv.eventmangement.application.services.BookingNumberGenerator;
import at.fhv.eventmangement.application.services.EventBookingService;
import at.fhv.eventmangement.domain.model.*;
import at.fhv.eventmangement.domain.repositories.CustomerRepository;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import at.fhv.eventmangement.domain.repositories.EventDateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
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
    @Transactional(rollbackFor = Exception.class)
    public EventBookingDto book(UUID eventId, EventBookingForm form)
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
                .customer(currentCustomer())
                .eventBookingType(form.getEventBookingType())
                .places(places)
                .price(event.getPrice().multiply(BigDecimal.valueOf(places)))
                .paymentMethod(form.getPaymentMethod())
                .eventBookingStatus(EventBookingStatus.CONFIRMED)
                .build();

        return eventBookingMapper.toDto(eventBookingRepository.save(booking));
    }

    @Override
    public List<String> getBookingTypes() {
        return Arrays.stream(EventBookingType.values()).map(Enum::name).toList();
    }

    @Override
    public List<String> getPaymentMethods() {
        return Arrays.stream(PaymentMethod.values()).map(Enum::name).toList();
    }

    // Only until there is a login
    //every booking belongs to the default customer!!
    private Customer currentCustomer() throws CustomerNotFoundException {
        return customerRepository.findByContactInfoName(EventmanagementApplicationRunner.DEFAULT_CUSTOMER)
                .orElseThrow(CustomerNotFoundException::new);
    }
}
