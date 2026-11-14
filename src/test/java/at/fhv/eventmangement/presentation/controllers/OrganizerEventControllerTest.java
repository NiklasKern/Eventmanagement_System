package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.*;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.application.services.EventCategoryService;
import at.fhv.eventmangement.application.services.EventService;
import at.fhv.eventmangement.domain.exceptions.EventAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Web layer tests for the organizer Thymeleaf controller.
 */
@WebMvcTest(OrganizerEventController.class)
class OrganizerEventControllerTest {

    private static final String MANDATORY = "please fill mandatory fields";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;
    @MockitoBean
    private EventCategoryService eventCategoryService;

    private final UUID categoryId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(eventCategoryService.getAllEventCategories())
                .thenReturn(List.of(new EventCategoryDto(categoryId, "Hiking")));
    }

    private static String futureStart() {
        return LocalDateTime.now().plusDays(10).withSecond(0).withNano(0).toString();
    }

    private static String futureEnd() {
        return LocalDateTime.now().plusDays(10).plusHours(3).withSecond(0).withNano(0).toString();
    }

    // "Create Event" button opens the form

    @Test
    void eventList_hasCreateEventButtonLinkingToTheForm() throws Exception {
        when(eventService.getAllEvents()).thenReturn(List.of());

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("my_events"))
                .andExpect(content().string(containsString("Create event")))
                .andExpect(content().string(containsString("href=\"/event/manage/create\"")));
    }

    @Test
    void createEventForm_opensWithEmptyFormAndCategories() throws Exception {
        mockMvc.perform(get("/event/manage/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("add_event"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attribute("categories", List.of(new EventCategoryDto(categoryId, "Hiking"))))
                .andExpect(content().string(containsString("Create an Event")))
                .andExpect(content().string(containsString("Hiking")));
    }


    @Test
    void submitEmptyForm_showsFormAgainWithMandatoryMessage() throws Exception {
        mockMvc.perform(post("/event/manage/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("add_event"))
                .andExpect(model().attributeHasFieldErrors("event",
                        "name", "location", "eventDates", "price",
                        "minParticipants", "maxParticipants", "eventCategoryId"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(content().string(containsString(MANDATORY)));

        verifyNoInteractions(eventService);
    }

    @Test
    void submitWithoutName_rejectsOnlyName() throws Exception {
        mockMvc.perform(post("/event/manage/create")
                        .param("location", "Dornbirn")
                        .param("eventDates[0].startTime", futureStart())
                        .param("eventDates[0].endTime", futureEnd())
                        .param("price", "25")
                        .param("minParticipants", "3")
                        .param("maxParticipants", "20")
                        .param("eventCategoryId", categoryId.toString()))
                .andExpect(view().name("add_event"))
                .andExpect(model().attributeErrorCount("event", 1))
                .andExpect(model().attributeHasFieldErrorCode("event", "name", "NotBlank"))
                .andExpect(content().string(containsString(MANDATORY)));

        verifyNoInteractions(eventService);
    }

    @Test
    void submitWithZeroPriceAndCapacity_isRejected() throws Exception {
        mockMvc.perform(post("/event/manage/create")
                        .param("name", "Summer Hike")
                        .param("location", "Dornbirn")
                        .param("eventDates[0].startTime", futureStart())
                        .param("eventDates[0].endTime", futureEnd())
                        .param("price", "0")
                        .param("minParticipants", "3")
                        .param("maxParticipants", "0")
                        .param("eventCategoryId", categoryId.toString()))
                .andExpect(view().name("add_event"))
                .andExpect(model().attributeHasFieldErrorCode("event", "price", "Positive"))
                .andExpect(model().attributeHasFieldErrorCode("event", "maxParticipants", "Positive"))
                .andExpect(content().string(containsString("must be greater than 0")));

        verifyNoInteractions(eventService);
    }

    @Test
    void submitWithNonNumericPrice_isRejected() throws Exception {
        mockMvc.perform(post("/event/manage/create")
                        .param("name", "Summer Hike")
                        .param("location", "Dornbirn")
                        .param("eventDates[0].startTime", futureStart())
                        .param("eventDates[0].endTime", futureEnd())
                        .param("price", "abc")
                        .param("minParticipants", "3")
                        .param("maxParticipants", "20")
                        .param("eventCategoryId", categoryId.toString()))
                .andExpect(view().name("add_event"))
                .andExpect(model().attributeHasFieldErrors("event", "price"));

        verifyNoInteractions(eventService);
    }

    @Test
    void submitWhenServiceRejectsInput_showsServiceMessageOnTheField() throws Exception {
        when(eventService.addEvent(any(EventForm.class)))
                .thenThrow(new EventAlreadyExistsException());

        mockMvc.perform(post("/event/manage/create")
                        .param("name", "Summer Hike")
                        .param("location", "Dornbirn")
                        .param("eventDates[0].startTime", futureStart())
                        .param("eventDates[0].endTime", futureEnd())
                        .param("price", "25")
                        .param("minParticipants", "3")
                        .param("maxParticipants", "20")
                        .param("eventCategoryId", categoryId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("add_event"))
                .andExpect(model().attributeHasFieldErrorCode("event", "name", "duplicate"))
                .andExpect(content().string(containsString("event name already exists")));
    }


    @Test
    void submitValidForm_savesAndRedirectsToListWithSuccessMessage() throws Exception {
        mockMvc.perform(post("/event/manage/create")
                        .param("name", "Summer Hike")
                        .param("description", "A nice walk")
                        .param("location", "Dornbirn")
                        .param("eventDates[0].startTime", futureStart())
                        .param("eventDates[0].endTime", futureEnd())
                        .param("price", "25.50")
                        .param("minParticipants", "3")
                        .param("maxParticipants", "20")
                        .param("eventCategoryId", categoryId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"))
                .andExpect(flash().attribute("successMessage", "Event successfully saved"));

        verify(eventService).addEvent(argThat(form ->
                form.getName().equals("Summer Hike")
                        && form.getLocation().equals("Dornbirn")
                        && form.getPrice().compareTo(new BigDecimal("25.50")) == 0
                        && form.getMaxParticipants() == 20
                        && form.getEventCategoryId().equals(categoryId)));
    }

    @Test
    void eventList_showsSuccessMessageAfterRedirect() throws Exception {
        when(eventService.getAllEvents()).thenReturn(List.of());

        mockMvc.perform(get("/events").flashAttr("successMessage", "Event successfully saved"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Event successfully saved")));
    }

    @Test
    void eventList_withoutFlash_showsNoSuccessMessage() throws Exception {
        when(eventService.getAllEvents()).thenReturn(List.of());

        mockMvc.perform(get("/events"))
                .andExpect(content().string(not(containsString("Event successfully saved"))));
    }

    @Test
    void eventList_showsEventsFromService() throws Exception {
        EventDto event = new EventDto(UUID.randomUUID(), "Summer Hike", "A nice walk", "Dornbirn",
                new BigDecimal("150"), 5, 50,"OPEN",
                new EventCategoryDto(categoryId, "Hiking"),
                new EventOrganizerDto(UUID.randomUUID(), new ContactInfoDto("Alpine Club", "Street 1", "a@b.at")),
                List.of(new EventDateDto(UUID.randomUUID(), LocalDateTime.of(2027, 1, 10, 17, 0),
                                                            LocalDateTime.of(2027, 1, 10, 20, 0)))
        );

        when(eventService.getAllEvents()).thenReturn(List.of(event));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("events", List.of(event)))
                .andExpect(content().string(containsString("Summer Hike")))
                .andExpect(content().string(containsString("Dornbirn")))
                .andExpect(content().string(containsString("10.01.2027, 17:00")))
                .andExpect(content().string(containsString("150,00 €")))
                .andExpect(content().string(containsString("Alpine Club")))
                .andExpect(content().string(not(containsString("You have not created any events yet."))));
    }

    @Test
    void eventList_withoutEvents_showsEmptyState() throws Exception {
        when(eventService.getAllEvents()).thenReturn(List.of());

        mockMvc.perform(get("/events"))
                .andExpect(content().string(containsString("You have not created any events yet.")));
    }
}
