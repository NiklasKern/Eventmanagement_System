package at.fhv.eventmangement.infrastructure.mappers;

import at.fhv.eventmangement.domain.model.Event;
import at.fhv.eventmangement.domain.model.EventCategory;
import at.fhv.eventmangement.domain.model.EventOrganizer;
import at.fhv.eventmangement.domain.model.EventStatus;
import at.fhv.eventmangement.infrastructure.entities.EventCategoryEntity;
import at.fhv.eventmangement.infrastructure.entities.EventEntity;
import at.fhv.eventmangement.infrastructure.entities.EventOrganizerEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Entity <-> domain model mapping

@SpringJUnitConfig(classes = {
        EventEntityMapperImpl.class,
        EventCategoryEntityMapperImpl.class,
        EventOrganizerEntityMapperImpl.class})
class EventEntityMapperTest {

    @Autowired
    private EventEntityMapper eventMapper;
    @Autowired
    private EventCategoryEntityMapper categoryMapper;
    @Autowired
    private EventOrganizerEntityMapper organizerMapper;

    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void toDomain_copiesAllFieldsIncludingRelations() {
        EventCategoryEntity category = new EventCategoryEntity(UUID.randomUUID(), "Hiking", now, now);
        EventOrganizerEntity organizer = new EventOrganizerEntity(UUID.randomUUID(), "Alpine Club", "Street 1", "a@b.at", now, now);
        EventEntity entity = EventEntity.builder()
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

        Event event = eventMapper.toDomain(entity);

        assertThat(event.getId()).isEqualTo(entity.getId());
        assertThat(event.getName()).isEqualTo("Summer Hike");
        assertThat(event.getDescription()).isEqualTo("A nice walk");
        assertThat(event.getLocation()).isEqualTo("Dornbirn");
        assertThat(event.getPrice()).isEqualByComparingTo("25.00");
        assertThat(event.getMinParticipants()).isEqualTo(3);
        assertThat(event.getMaxParticipants()).isEqualTo(20);
        assertThat(event.getDateTime()).isEqualTo(entity.getDateTime());
        assertThat(event.getEventStatus()).isEqualTo(EventStatus.OPEN);
        assertThat(event.getCreated()).isEqualTo(now);
        assertThat(event.getUpdated()).isEqualTo(now);
        assertThat(event.getEventCategory().getId()).isEqualTo(category.getId());
        assertThat(event.getEventCategory().getCategoryName()).isEqualTo("Hiking");
        assertThat(event.getEventOrganizer().getId()).isEqualTo(organizer.getId());
        assertThat(event.getEventOrganizer().getOrganizerName()).isEqualTo("Alpine Club");
    }

    @Test
    void toEntity_copiesFieldsButLeavesRelationsToTheAdapter() {
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .name("Summer Hike")
                .location("Dornbirn")
                .price(new BigDecimal("25.00"))
                .minParticipants(3)
                .maxParticipants(20)
                .dateTime(now.plusDays(10))
                .eventStatus(EventStatus.OPEN)
                .eventCategory(new EventCategory(UUID.randomUUID(), "Hiking", now, now))
                .eventOrganizer(new EventOrganizer(UUID.randomUUID(), "Alpine Club", "Street 1", "a@b.at", now, now))
                .created(now)
                .updated(now)
                .build();

        EventEntity entity = eventMapper.toEntity(event);

        assertThat(entity.getId()).isEqualTo(event.getId());
        assertThat(entity.getName()).isEqualTo("Summer Hike");
        assertThat(entity.getEventStatus()).isEqualTo(EventStatus.OPEN);
        assertThat(entity.getCreated()).isEqualTo(now);
        // Relations are set by EventRepositoryAdapter via getReferenceById, never by the mapper
        assertThat(entity.getEventCategory()).isNull();
        assertThat(entity.getEventOrganizer()).isNull();
    }

    @Test
    void category_roundTripKeepsAllFields() {
        EventCategory category = new EventCategory(UUID.randomUUID(), "Hiking", now, now);

        EventCategory back = categoryMapper.toDomain(categoryMapper.toEntity(category));

        assertThat(back).usingRecursiveComparison().isEqualTo(category);
    }

    @Test
    void organizer_roundTripKeepsAllFields() {
        EventOrganizer organizer = new EventOrganizer(UUID.randomUUID(), "Alpine Club", "Street 1", "a@b.at", now, now);

        EventOrganizer back = organizerMapper.toDomain(organizerMapper.toEntity(organizer));

        assertThat(back).usingRecursiveComparison().isEqualTo(organizer);
    }
}
