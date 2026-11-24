package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.domain.exceptions.EventAlreadyCancelledException;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import at.fhv.eventmangement.domain.model.*;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import at.fhv.eventmangement.domain.repositories.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the database part of an event cancellation.
 */
@ExtendWith(MockitoExtension.class)
class EventCancelTransactionTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventBookingRepository eventBookingRepository;

    @InjectMocks
    private EventCancelTransaction eventCancelTransaction;

    private UUID eventId;
    private Event event;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        event = Event.builder()
                .id(eventId)
                .name("Summer Hike")
                .eventStatus(EventStatus.ACTIVE)
                .build();
    }

    private static Customer customer(String name) {
        return Customer.builder()
                .id(UUID.randomUUID())
                .contactInfo(new ContactInfo(name, "Street 1", name.toLowerCase() + "@test.com"))
                .build();
    }

    @Test
    void apply_cancelsEventAndActiveBookings_andDeduplicatesCustomers() throws Exception {
        Customer anna = customer("Anna");
        Customer bernd = customer("Bernd");

        // Anna has two bookings, Bernd has one the result should contain both customers only once.
        EventBooking booking1 = EventBooking.builder().customer(anna).build();
        EventBooking booking2 = EventBooking.builder().customer(anna).build();
        EventBooking booking3 = EventBooking.builder().customer(bernd).build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventBookingRepository.findActiveBookingsByEventId(eventId))
                .thenReturn(List.of(booking1, booking2, booking3));

        EventCancelTransaction.Result result = eventCancelTransaction.apply(eventId);

        assertThat(result.event().getEventStatus()).isEqualTo(EventStatus.CANCELLED);
        assertThat(booking1.getEventBookingStatus()).isEqualTo(EventBookingStatus.CANCELLED);
        assertThat(booking1.getCancellationDate()).isEqualTo(LocalDate.now());
        assertThat(booking2.getEventBookingStatus()).isEqualTo(EventBookingStatus.CANCELLED);
        assertThat(booking3.getEventBookingStatus()).isEqualTo(EventBookingStatus.CANCELLED);
        assertThat(result.customersToNotify()).containsExactly(anna, bernd);

        verify(eventRepository).save(event);
        verify(eventBookingRepository, times(3)).save(any(EventBooking.class));
    }

    @Test
    void apply_eventWithoutBookings_cancelsEventAndReturnsEmptyCustomerList() throws Exception {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventBookingRepository.findActiveBookingsByEventId(eventId)).thenReturn(List.of());

        EventCancelTransaction.Result result = eventCancelTransaction.apply(eventId);

        assertThat(result.event().getEventStatus()).isEqualTo(EventStatus.CANCELLED);
        assertThat(result.customersToNotify()).isEmpty();
        verify(eventBookingRepository, never()).save(any());
    }

    @Test
    void apply_unknownEventId_throwsEventNotFoundExceptionAndDoesNotSave() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventCancelTransaction.apply(eventId))
                .isInstanceOf(EventNotFoundException.class);
        verify(eventRepository, never()).save(any());
        verifyNoInteractions(eventBookingRepository);
    }

    @Test
    void apply_alreadyCancelledEvent_throwsEventAlreadyCancelledExceptionAndDoesNotSave() {
        event.setEventStatus(EventStatus.CANCELLED);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventCancelTransaction.apply(eventId))
                .isInstanceOf(EventAlreadyCancelledException.class);
        verify(eventRepository, never()).save(any());
        verifyNoInteractions(eventBookingRepository);
    }
}