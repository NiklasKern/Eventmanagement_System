package at.fhv.eventmangement.presentation.controllers;

import at.fhv.eventmangement.application.dto.*;
import at.fhv.eventmangement.domain.exceptions.EventAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventNotBookableException;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import at.fhv.eventmangement.domain.exceptions.NotEnoughPlacesException;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.application.services.CustomerService;
import at.fhv.eventmangement.application.services.EventBookingService;
import at.fhv.eventmangement.application.services.EventCategoryService;
import at.fhv.eventmangement.application.services.EventService;
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

import static org.mockito.Mockito.never;

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
 * Web layer tests for the Thymeleaf controller.
 */
@WebMvcTest(EventController.class)
class EventControllerTest {

    private static final String MANDATORY = "please fill mandatory fields";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;
    @MockitoBean
    private EventCategoryService eventCategoryService;
    @MockitoBean
    private EventBookingService eventBookingService;
    @MockitoBean
    private CustomerService customerService;

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

    // validation: "please fill mandatory fields"

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

    // ---------- success: redirect + toast message + updated list ----------

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

    // ---------- booking ----------

    private final UUID eventId = UUID.randomUUID();
    private final UUID eventDateId = UUID.randomUUID();

    private EventDto bookableEvent() {
        return new EventDto(eventId, "Summer Hike", "A nice walk", "Dornbirn",
                new BigDecimal("25"), 1, 10, "ACTIVE",
                new EventCategoryDto(categoryId, "Hiking"),
                new EventOrganizerDto(UUID.randomUUID(), new ContactInfoDto("Alpine Club", "Street 1", "a@b.at")),
                List.of(new EventDateDto(eventDateId, LocalDateTime.of(2027, 1, 10, 17, 0),
                                                      LocalDateTime.of(2027, 1, 10, 20, 0))));
    }

    private CustomerDto defaultCustomer() {
        return new CustomerDto(UUID.randomUUID(), new ContactInfoDto("test-customer", "Street 2", "c@test.com"),
                null, null);
    }

    private org.springframework.test.web.servlet.ResultActions postGroupBooking(int places) throws Exception {
        return mockMvc.perform(post("/customer/event/{id}/book", eventId)
                .param("eventDateId", eventDateId.toString())
                .param("eventBookingType", "GROUP")
                .param("places", String.valueOf(places))
                .param("paymentMethod", "CARD"));
    }

    @Test
    void bookingForm_showsEventAndCustomer() throws Exception {
        when(eventService.getEventById(eventId)).thenReturn(bookableEvent());
        when(customerService.getCurrentCustomer()).thenReturn(defaultCustomer());

        mockMvc.perform(get("/customer/event/{id}/book", eventId))
                .andExpect(status().isOk())
                .andExpect(view().name("customer_events"))
                .andExpect(content().string(containsString("Book Summer Hike")))
                .andExpect(content().string(containsString("test-customer")));
    }

    @Test
    void bookingForm_unknownEvent_redirectsToEventListWithError() throws Exception {
        when(customerService.getCurrentCustomer()).thenReturn(defaultCustomer());
        when(eventService.getEventById(eventId)).thenThrow(new EventNotFoundException());

        mockMvc.perform(get("/customer/event/{id}/book", eventId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("errorMessage", "event does not exist"));
    }

    @Test
    void bookingForm_missingDefaultCustomer_redirectsToEventListWithError() throws Exception {
        when(customerService.getCurrentCustomer()).thenThrow(new CustomerNotFoundException());

        mockMvc.perform(get("/customer/event/{id}/book", eventId))
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("errorMessage", "customer does not exist"));
    }

    @Test
    void book_success_redirectsWithBookingNumber() throws Exception {
        when(eventBookingService.book(any(), any())).thenReturn(new EventBookingDto(
                UUID.randomUUID(), "BK-ABCDEFGH", null, null, null, 2, null, null, null, null, null, List.of()));

        postGroupBooking(2)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("successMessage", "Event successfully booked (BK-ABCDEFGH)"));
    }

    @Test
    void book_notEnoughPlaces_showsErrorOnPlacesField() throws Exception {
        when(eventBookingService.book(any(), any())).thenThrow(new NotEnoughPlacesException(3));
        when(eventService.getEventById(eventId)).thenReturn(bookableEvent());

        postGroupBooking(4)
                .andExpect(status().isOk())
                .andExpect(view().name("customer_events"))
                .andExpect(model().attributeHasFieldErrorCode("booking", "places", "notEnoughPlaces"))
                .andExpect(content().string(containsString("only 3 places left")));
    }

    @Test
    void book_eventNotBookable_showsErrorOnDateField() throws Exception {
        when(eventBookingService.book(any(), any())).thenThrow(new EventNotBookableException());
        when(eventService.getEventById(eventId)).thenReturn(bookableEvent());

        postGroupBooking(2)
                .andExpect(view().name("customer_events"))
                .andExpect(model().attributeHasFieldErrorCode("booking", "eventDateId", "notBookable"))
                .andExpect(content().string(containsString("event is not open for booking")));
    }

    @Test
    void book_missingDefaultCustomer_showsGlobalError() throws Exception {
        when(eventBookingService.book(any(), any())).thenThrow(new CustomerNotFoundException());
        when(eventService.getEventById(eventId)).thenReturn(bookableEvent());

        postGroupBooking(2)
                .andExpect(view().name("customer_events"))
                .andExpect(model().attributeHasErrors("booking"))
                .andExpect(content().string(containsString("customer does not exist")));
    }

    @Test
    void book_invalidForm_doesNotCallService() throws Exception {
        when(eventService.getEventById(eventId)).thenReturn(bookableEvent());

        mockMvc.perform(post("/customer/event/{id}/book", eventId))
                .andExpect(view().name("customer_events"))
                .andExpect(model().attributeHasFieldErrors("booking", "eventDateId", "eventBookingType", "paymentMethod"));

        verify(eventBookingService, never()).book(any(), any());
    }

    @Test
    void book_eventDisappearedWhileShowingErrors_redirectsWithError() throws Exception {
        when(eventBookingService.book(any(), any())).thenThrow(new NotEnoughPlacesException(0));
        when(eventService.getEventById(eventId)).thenThrow(new EventNotFoundException());

        postGroupBooking(2)
                .andExpect(redirectedUrl("/customer/events"))
                .andExpect(flash().attribute("errorMessage", "event does not exist"));
    }

    @Test
    void customerEvents_showsErrorFlash() throws Exception {
        when(eventService.getActiveEvents()).thenReturn(List.of());

        mockMvc.perform(get("/customer/events").flashAttr("errorMessage", "event does not exist"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FlashError")))
                .andExpect(content().string(containsString("event does not exist")));
    }
}
