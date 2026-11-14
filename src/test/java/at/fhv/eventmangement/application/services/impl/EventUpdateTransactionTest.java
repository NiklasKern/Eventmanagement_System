package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.form.EventDateForm;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.domain.exceptions.EventDateHasBookingsException;
import at.fhv.eventmangement.domain.exceptions.MaxParticipantsBelowBookedPlacesException;
import at.fhv.eventmangement.domain.model.*;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import at.fhv.eventmangement.domain.repositories.EventCategoryRepository;
import at.fhv.eventmangement.domain.repositories.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the database part of an event update.
 */
@ExtendWith(MockitoExtension.class)
class EventUpdateTransactionTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventCategoryRepository eventCategoryRepository;
    @Mock
    private EventBookingRepository eventBookingRepository;

    @InjectMocks
    private EventUpdateTransaction eventUpdateTransaction;

    private UUID eventId;
    private EventDate date;
    private EventCategory category;
    private Event event;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        category = EventCategory.builder().id(UUID.randomUUID()).categoryName("Hiking").build();
        date = EventDate.builder()
                .id(UUID.randomUUID())
                .startTime(LocalDateTime.now().plusDays(10))
                .endTime(LocalDateTime.now().plusDays(10).plusHours(3))
                .build();
        event = Event.builder()
                .id(eventId)
                .name("Summer Hike")
                .maxParticipants(20)
                .eventCategory(category)
                .eventDates(new ArrayList<>(List.of(date)))
                .build();
        date.setEvent(event);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventCategoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
    }

    private EventForm form(int maxParticipants, List<EventDateForm> dates) {
        return EventForm.builder()
                .name("Summer Hike")
                .location("Dornbirn")
                .price(new BigDecimal("25.00"))
                .minParticipants(1)
                .maxParticipants(maxParticipants)
                .eventCategoryId(category.getId())
                .eventDates(new ArrayList<>(dates))
                .build();
    }

    private EventDateForm keepDate() {
        return EventDateForm.builder().id(date.getId()).startTime(date.getStartTime()).endTime(date.getEndTime()).build();
    }

    @Test
    void apply_maxParticipantsBelowBookedPlaces_throwsAndDoesNotSave() {
        when(eventBookingRepository.sumBookedPlaces(date.getId())).thenReturn(12);

        assertThatThrownBy(() -> eventUpdateTransaction.apply(eventId, form(10, List.of(keepDate()))))
                .isInstanceOf(MaxParticipantsBelowBookedPlacesException.class)
                .hasMessageContaining("12");
        verify(eventRepository, never()).save(any());
    }

    @Test
    void apply_maxParticipantsEqualToBookedPlaces_isAllowed() throws Exception {
        when(eventBookingRepository.sumBookedPlaces(date.getId())).thenReturn(10);
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EventUpdateTransaction.Result result = eventUpdateTransaction.apply(eventId, form(10, List.of(keepDate())));

        assertThat(result.event().getMaxParticipants()).isEqualTo(10);
    }

    @Test
    void apply_removingDateWithBookings_throwsAndDoesNotSave() {
        when(eventBookingRepository.hasActiveBookings(date.getId())).thenReturn(true);
        EventDateForm newDate = EventDateForm.builder()
                .startTime(LocalDateTime.now().plusDays(20))
                .endTime(LocalDateTime.now().plusDays(20).plusHours(2))
                .build();

        assertThatThrownBy(() -> eventUpdateTransaction.apply(eventId, form(20, List.of(newDate))))
                .isInstanceOf(EventDateHasBookingsException.class);
        verify(eventRepository, never()).save(any());
    }
}
