package at.fhv.eventmangement.presentation.rest;

import at.fhv.eventmangement.application.dto.ContactInfoDto;
import at.fhv.eventmangement.application.dto.EventCategoryDto;
import at.fhv.eventmangement.application.dto.EventDateDto;
import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.dto.EventOrganizerDto;
import at.fhv.eventmangement.application.services.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REST API tests (GlobalExceptionHandler).
 */
@WebMvcTest(EventRestController.class)
class EventRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Test
    void getEvents_returnsEventsAsJson() throws Exception {
        UUID id = UUID.randomUUID();
        EventDto event = new EventDto(id, "Summer Hike", "A nice walk", "Dornbirn",
                new BigDecimal("150.00"), 5, 50, "ACTIVE",
                new EventCategoryDto(UUID.randomUUID(), "Hiking"),
                new EventOrganizerDto(UUID.randomUUID(), new ContactInfoDto("Alpine Club", "Street 1", "a@b.at")),
                List.of(new EventDateDto(UUID.randomUUID(),
                        LocalDateTime.of(2027, 1, 10, 17, 0),
                        LocalDateTime.of(2027, 1, 10, 20, 0))));
        when(eventService.getAllEvents()).thenReturn(List.of(event));

        mockMvc.perform(get("/getEvents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Summer Hike"))
                .andExpect(jsonPath("$[0].location").value("Dornbirn"))
                .andExpect(jsonPath("$[0].price").value(150.00))
                .andExpect(jsonPath("$[0].minParticipants").value(5))
                .andExpect(jsonPath("$[0].maxParticipants").value(50))
                .andExpect(jsonPath("$[0].eventStatus").value("ACTIVE"))
                .andExpect(jsonPath("$[0].eventCategory.categoryName").value("Hiking"))
                .andExpect(jsonPath("$[0].eventOrganizer.contactInfo.name").value("Alpine Club"))
                .andExpect(jsonPath("$[0].eventDates.length()").value(1))
                .andExpect(jsonPath("$[0].eventDates[0].startTime").value("2027-01-10T17:00:00"));
    }

    @Test
    void getEvents_returnsEmptyArrayWhenNoEvents() throws Exception {
        when(eventService.getAllEvents()).thenReturn(List.of());

        mockMvc.perform(get("/getEvents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void unexpectedError_returns500WithoutInternalDetails() throws Exception {
        when(eventService.getAllEvents()).thenThrow(new IllegalStateException("database password is wrong"));

        mockMvc.perform(get("/getEvents"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"));
    }
}
