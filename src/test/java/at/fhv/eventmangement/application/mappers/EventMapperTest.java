package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.domain.model.Event;
import at.fhv.eventmangement.domain.model.EventCategory;
import at.fhv.eventmangement.domain.model.EventOrganizer;
import at.fhv.eventmangement.domain.model.EventStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Domain model -> DTO mapping.
 */
@SpringJUnitConfig(classes = {EventMapperImpl.class, EventCategoryMapperImpl.class, EventOrganizerMapperImpl.class})
class EventMapperTest {

    @Autowired
    private EventMapper eventMapper;

    @Test
    void toDto_copiesAllFieldsAndNestedObjects() {
        LocalDateTime now = LocalDateTime.now();
        EventCategory category = new EventCategory(UUID.randomUUID(), "Hiking", now, now);
        EventOrganizer organizer = new EventOrganizer(UUID.randomUUID(), "Alpine Club", "Street 1", "a@b.at", now, now);
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .name("Summer Hike")
                .description("A nice walk")
                .location("Dornbirn")
                .price(new BigDecimal("25.00"))
                .minParticipants(3)
                .maxParticipants(20)
                .dateTime(now.plusDays(10))
                .eventStatus(EventStatus.OPEN)
                .eventCategory(category)
                .eventOrganizer(organizer)
                .created(now)
                .updated(now)
                .build();

        EventDto dto = eventMapper.toDto(event);

        assertThat(dto.id()).isEqualTo(event.getId());
        assertThat(dto.name()).isEqualTo("Summer Hike");
        assertThat(dto.description()).isEqualTo("A nice walk");
        assertThat(dto.location()).isEqualTo("Dornbirn");
        assertThat(dto.price()).isEqualByComparingTo("25.00");
        assertThat(dto.minParticipants()).isEqualTo(3);
        assertThat(dto.maxParticipants()).isEqualTo(20);
        assertThat(dto.dateTime()).isEqualTo(event.getDateTime());
        assertThat(dto.eventCategory().id()).isEqualTo(category.getId());
        assertThat(dto.eventCategory().categoryName()).isEqualTo("Hiking");
        assertThat(dto.eventOrganizer().id()).isEqualTo(organizer.getId());
        assertThat(dto.eventOrganizer().organizerName()).isEqualTo("Alpine Club");
        assertThat(dto.eventOrganizer().address()).isEqualTo("Street 1");
        assertThat(dto.eventOrganizer().email()).isEqualTo("a@b.at");
    }

    @Test
    void toDto_turnsStatusEnumIntoPlainString() {
        Event event = Event.builder().name("X").eventStatus(EventStatus.CLOSED).build();

        assertThat(eventMapper.toDto(event).eventStatus()).isEqualTo("CLOSED");
    }

    @Test
    void toDto_handlesMissingRelations() {
        Event event = Event.builder().name("X").build();

        EventDto dto = eventMapper.toDto(event);

        assertThat(dto.eventCategory()).isNull();
        assertThat(dto.eventOrganizer()).isNull();
        assertThat(dto.eventStatus()).isNull();
    }
}
