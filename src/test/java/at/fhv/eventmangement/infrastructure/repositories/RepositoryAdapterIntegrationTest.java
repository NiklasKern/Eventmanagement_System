package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.TestcontainersConfiguration;
import at.fhv.eventmangement.domain.model.ContactInfo;
import at.fhv.eventmangement.domain.model.Event;
import at.fhv.eventmangement.domain.model.EventCategory;
import at.fhv.eventmangement.domain.model.EventDate;
import at.fhv.eventmangement.domain.model.EventOrganizer;
import at.fhv.eventmangement.domain.model.EventStatus;
import at.fhv.eventmangement.domain.repositories.EventCategoryRepository;
import at.fhv.eventmangement.domain.repositories.EventOrganizerRepository;
import at.fhv.eventmangement.domain.repositories.EventRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Tests the repository adapters against PostgreSQL.

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class RepositoryAdapterIntegrationTest {

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventCategoryRepository categoryRepository;
    @Autowired
    private EventOrganizerRepository organizerRepository;
    @Autowired
    private EntityManager entityManager;

    // PostgreSQL stores microseconds, Java has nanoseconds -> truncate so values compare equal
    private final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

    private EventCategory saveCategory(String name) {
        return categoryRepository.save(new EventCategory(name));
    }

    private EventOrganizer saveOrganizer(String name) {
        return organizerRepository.save(new EventOrganizer(new ContactInfo(name, "Street 1", "a@b.at")));
    }

    private Event newEvent(String name, EventCategory category, EventOrganizer organizer) {
        return Event.builder()
                .name(name)
                .description("A nice walk")
                .location("Dornbirn")
                .price(new BigDecimal("25.50"))
                .minParticipants(3)
                .maxParticipants(20)
                .eventDates(List.of(EventDate.builder()
                        .startTime(now.plusDays(10))
                        .endTime(now.plusDays(10).plusHours(3))
                        .build()))
                .eventStatus(EventStatus.ACTIVE)
                .eventCategory(category)
                .eventOrganizer(organizer)
                .build();
    }

    // categories

    @Test
    void category_saveGeneratesIdAndCanBeFoundAgain() {
        EventCategory saved = saveCategory("Climbing");
        // write the INSERT and reload from the DB, otherwise the timestamp is not set yet
        entityManager.flush();
        entityManager.clear();

        assertThat(saved.getId()).isNotNull();
        assertThat(categoryRepository.findById(saved.getId()))
                .get()
                .satisfies(found -> {
                    assertThat(found.getCategoryName()).isEqualTo("Climbing");
                    assertThat(found.getCreated()).as("set by @CreationTimestamp").isNotNull();
                });
        assertThat(categoryRepository.findAll())
                .extracting(EventCategory::getCategoryName)
                .contains("Climbing");
    }

    @Test
    void category_existsCheckIgnoresCase() {
        saveCategory("Climbing");

        assertThat(categoryRepository.existsByCategoryNameIgnoreCase("climbing")).isTrue();
        assertThat(categoryRepository.existsByCategoryNameIgnoreCase("CLIMBING")).isTrue();
        assertThat(categoryRepository.existsByCategoryNameIgnoreCase("Skiing")).isFalse();
    }

    // organizers

    @Test
    void organizer_saveGeneratesIdAndCanBeFoundByName() {
        EventOrganizer saved = saveOrganizer("Alpine Club");

        assertThat(saved.getId()).isNotNull();
        assertThat(organizerRepository.findByContactInfoName("Alpine Club"))
                .get()
                .satisfies(found -> {
                    assertThat(found.getId()).isEqualTo(saved.getId());
                    assertThat(found.getContactInfo().getAddress()).isEqualTo("Street 1");
                    assertThat(found.getContactInfo().getEmail()).isEqualTo("a@b.at");
                });
        assertThat(organizerRepository.findByContactInfoName("Nobody")).isEmpty();
    }

    @Test
    void organizer_existsCheckIgnoresCase() {
        saveOrganizer("Alpine Club");

        assertThat(organizerRepository.existsByContactInfoNameIgnoreCase("alpine club")).isTrue();
        assertThat(organizerRepository.existsByContactInfoNameIgnoreCase("Other Club")).isFalse();
    }

    // events

    @Test
    void event_saveReturnsEventWithGeneratedIdAndRelations() {
        EventCategory category = saveCategory("Climbing");
        EventOrganizer organizer = saveOrganizer("Alpine Club");

        Event saved = eventRepository.save(newEvent("Summer Hike", category, organizer));

        assertThat(saved.getId()).as("adapter must return the saved event, with the DB id").isNotNull();
        assertThat(saved.getEventCategory().getId()).isEqualTo(category.getId());
        assertThat(saved.getEventCategory().getCategoryName()).isEqualTo("Climbing");
        assertThat(saved.getEventOrganizer().getId()).isEqualTo(organizer.getId());
        assertThat(saved.getEventOrganizer().getContactInfo().getName()).isEqualTo("Alpine Club");
    }

    @Test
    void event_findAllReturnsAllFieldsAndRelations() {
        EventCategory category = saveCategory("Climbing");
        EventOrganizer organizer = saveOrganizer("Alpine Club");
        Event saved = eventRepository.save(newEvent("Summer Hike", category, organizer));

        assertThat(eventRepository.findAll())
                .filteredOn(e -> e.getId().equals(saved.getId()))
                .singleElement()
                .satisfies(found -> {
                    assertThat(found.getName()).isEqualTo("Summer Hike");
                    assertThat(found.getDescription()).isEqualTo("A nice walk");
                    assertThat(found.getLocation()).isEqualTo("Dornbirn");
                    assertThat(found.getPrice()).isEqualByComparingTo("25.50");
                    assertThat(found.getMinParticipants()).isEqualTo(3);
                    assertThat(found.getMaxParticipants()).isEqualTo(20);
                    assertThat(found.getEventDates()).singleElement().satisfies(d -> {
                        assertThat(d.getStartTime()).isEqualTo(now.plusDays(10));
                        assertThat(d.getEndTime()).isEqualTo(now.plusDays(10).plusHours(3));
                    });
                    assertThat(found.getEventStatus()).isEqualTo(EventStatus.ACTIVE);
                    assertThat(found.getEventCategory().getCategoryName()).isEqualTo("Climbing");
                    assertThat(found.getEventOrganizer().getContactInfo().getName()).isEqualTo("Alpine Club");
                });
    }

    @Test
    void event_existsByName() {
        Event saved = eventRepository.save(
                newEvent("Summer Hike", saveCategory("Climbing"), saveOrganizer("Alpine Club")));

        assertThat(eventRepository.existsByName(saved.getName())).isTrue();
        assertThat(eventRepository.existsByName("Winter Hike")).isFalse();
    }
}
