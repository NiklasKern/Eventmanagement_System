package at.fhv.eventmangement;

import at.fhv.eventmangement.application.dto.EventCategoryDto;
import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.domain.exceptions.EventCategoryAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.EventOrganizerAlreadyExistsException;
import at.fhv.eventmangement.application.form.EventCategoryForm;
import at.fhv.eventmangement.application.form.EventOrganizerForm;
import at.fhv.eventmangement.application.services.EventCategoryService;
import at.fhv.eventmangement.application.services.EventOrganizerService;
import at.fhv.eventmangement.application.services.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Test of the "Create Event" user story
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class CreateEventAcceptanceTest {

    private static final String CATEGORY = "Hiking";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EventService eventService;
    @Autowired
    private EventCategoryService categoryService;
    @Autowired
    private EventOrganizerService organizerService;

    private UUID categoryId;

    @BeforeEach
    void ensureReferenceData() throws Exception {
        organizerService.addOrganizer(new EventOrganizerForm(
                EventmanagementApplicationRunner.DEFAULT_ORGANIZER, "Street 1", "test@test.com"));

        categoryService.addCategory(new EventCategoryForm(CATEGORY));

        categoryId = categoryService.getAllEventCategories().stream()
                .filter(c -> c.categoryName().equals(CATEGORY))
                .map(EventCategoryDto::id)
                .findFirst()
                .orElseThrow();
    }

    private static String uniqueName() {
        return "Event " + UUID.randomUUID().toString().substring(0, 8);
    }

    private MockHttpServletRequestBuilder validPost(String name) {
        return postWithParticipants(name, 3, 20);
    }

    private MockHttpServletRequestBuilder postWithParticipants(String name, int min, int max) {
        return post("/event/manage/create")
                .param("name", name)
                .param("description", "A nice walk")
                .param("location", "Dornbirn")
                .param("eventDates[0].startTime", LocalDateTime.now().plusDays(10).withSecond(0).withNano(0).toString())
                .param("eventDates[0].endTime", LocalDateTime.now().plusDays(10).plusHours(3).withSecond(0).withNano(0).toString())
                .param("price", "25.50")
                .param("minParticipants", String.valueOf(min))
                .param("maxParticipants", String.valueOf(max))
                .param("eventCategoryId", categoryId.toString());
    }

    @Test
    void createEvent_fullFlow_fromButtonToUpdatedList() throws Exception {
        String name = uniqueName();

        // 1. Event list offers the "Create event" button
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/event/manage/create\"")));

        // 2. The button opens the "Create Event" form
        mockMvc.perform(get("/event/manage/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("add_event"))
                .andExpect(content().string(containsString(CATEGORY)));

        // 3. Submitting a valid form saves and redirects with the success message
        MvcResult result = mockMvc.perform(validPost(name))
                .andExpect(redirectedUrl("/events"))
                .andExpect(flash().attribute("successMessage", "Event successfully saved"))
                .andReturn();

        // 4. The list after the redirect shows the message and the new event
        mockMvc.perform(get("/events").flashAttrs(result.getFlashMap()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Event successfully saved")))
                .andExpect(content().string(containsString(name)))
                .andExpect(content().string(containsString(CATEGORY)));

        // 5. And it really is in the database, with status ACTIVE and the default organizer
        assertThat(eventService.getAllEvents())
                .filteredOn(e -> e.name().equals(name))
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.id()).isNotNull();
                    assertThat(e.eventStatus()).isEqualTo("ACTIVE");
                    assertThat(e.eventDates()).hasSize(1);
                    assertThat(e.eventCategory().categoryName()).isEqualTo(CATEGORY);
                    assertThat(e.eventOrganizer().contactInfo().name())
                            .isEqualTo(EventmanagementApplicationRunner.DEFAULT_ORGANIZER);
                });
    }

    @Test
    void createEvent_withMissingMandatoryFields_savesNothing() throws Exception {
        int before = eventService.getAllEvents().size();

        mockMvc.perform(post("/event/manage/create").param("description", "only a description"))
                .andExpect(status().isOk())
                .andExpect(view().name("add_event"))
                .andExpect(content().string(containsString("please fill mandatory fields")));

        assertThat(eventService.getAllEvents()).hasSize(before);
    }

    @Test
    void createEvent_withExistingName_isRejectedAndNotSavedTwice() throws Exception {
        String name = uniqueName();
        mockMvc.perform(validPost(name)).andExpect(redirectedUrl("/events"));

        mockMvc.perform(validPost(name))
                .andExpect(status().isOk())
                .andExpect(view().name("add_event"))
                .andExpect(content().string(containsString("event name already exists")));

        assertThat(eventService.getAllEvents())
                .extracting(EventDto::name)
                .filteredOn(name::equals)
                .hasSize(1);
    }

    @Test
    void createEvent_withMinAboveMax_isRejected() throws Exception {
        String name = uniqueName();

        mockMvc.perform(postWithParticipants(name, 30, 20))
                .andExpect(view().name("add_event"))
                .andExpect(content().string(containsString("must not be less than minimum participants")));

        assertThat(eventService.getAllEvents()).extracting(EventDto::name).doesNotContain(name);
    }
}
