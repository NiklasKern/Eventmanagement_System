package at.fhv.eventmangement.domain.model;

import at.fhv.eventmangement.domain.exceptions.EventAlreadyCancelledException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventCancellationDomainTest {

    @Test
    void cancel_activeEvent_setsStatusToCancelled() throws EventAlreadyCancelledException {
        Event event = Event.builder()
                .name("Summer Hike")
                .eventStatus(EventStatus.ACTIVE)
                .build();

        event.cancel();

        assertThat(event.getEventStatus()).isEqualTo(EventStatus.CANCELLED);
        assertThat(event.isCancelled()).isTrue();
    }

    @Test
    void cancel_alreadyCancelledEvent_throwsException() {
        Event event = Event.builder()
                .name("Summer Hike")
                .eventStatus(EventStatus.CANCELLED)
                .build();

        assertThatThrownBy(event::cancel)
                .isInstanceOf(EventAlreadyCancelledException.class);
    }

    @Test
    void cancel_booking_setsStatusToCancelledAndRecordsDate() {
        EventBooking booking = EventBooking.builder()
                .bookingNumber("BK-1001")
                .build();

        booking.cancel();

        assertThat(booking.getEventBookingStatus()).isEqualTo(EventBookingStatus.CANCELLED);
        assertThat(booking.getCancellationDate()).isEqualTo(LocalDate.now());
    }
}