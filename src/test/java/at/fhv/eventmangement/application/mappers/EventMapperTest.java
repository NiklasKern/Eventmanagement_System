package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.ContactInfoDto;
import at.fhv.eventmangement.application.dto.EventDateDto;
import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Domain model -> DTO mapping.
 */
@SpringJUnitConfig(classes = {EventMapperImpl.class, EventCategoryMapperImpl.class, EventOrganizerMapperImpl.class,
        ContactInfoMapperImpl.class, EventDateMapperImpl.class})
class EventMapperTest {

    @Autowired
    private EventMapper eventMapper;

    @Test
    void toDto_copiesAllFieldsAndNestedObjects() {
        LocalDateTime now = LocalDateTime.now();
        EventCategory category = new EventCategory("Hiking");
        EventOrganizer organizer = new EventOrganizer(new ContactInfo("Alpine Club", "Street 1", "a@b.at"));
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .name("Summer Hike")
                .description("A nice walk")
                .location("Dornbirn")
                .price(new BigDecimal("25.00"))
                .minParticipants(3)
                .maxParticipants(20)
                .eventDates(List.of(EventDate.builder()
                        .startTime(now.plusDays(10))
                        .endTime(now.plusDays(10).plusHours(3))
                        .build()
                ))
                .eventStatus(EventStatus.ACTIVE)
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
        assertThat(dto.eventDates()).isEqualTo(List.of(new EventDateDto(
                null,
                now.plusDays(10),
                now.plusDays(10).plusHours(3)
        )));
        assertThat(dto.eventCategory().id()).isEqualTo(category.getId());
        assertThat(dto.eventCategory().categoryName()).isEqualTo("Hiking");
        assertThat(dto.eventOrganizer().id()).isEqualTo(organizer.getId());
        assertThat(dto.eventOrganizer().contactInfo()).isEqualTo(new ContactInfoDto("Alpine Club", "Street 1", "a@b.at"));
    }

    @Test
    void toDto_turnsStatusEnumIntoPlainString() {
        Event event = Event.builder().name("X").eventStatus(EventStatus.INACTIVE).build();

        assertThat(eventMapper.toDto(event).eventStatus()).isEqualTo("INACTIVE");
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
