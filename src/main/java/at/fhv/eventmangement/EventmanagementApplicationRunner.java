package at.fhv.eventmangement;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.form.*;
import at.fhv.eventmangement.application.services.*;
import at.fhv.eventmangement.domain.model.CustomerType;
import at.fhv.eventmangement.domain.model.EventBookingType;
import at.fhv.eventmangement.domain.model.PaymentMethod;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("local")
public class EventmanagementApplicationRunner implements ApplicationRunner {

    // Until there is a login, every event belongs to this organizer
    public static final String DEFAULT_ORGANIZER = "test-organizer";
    public static final String DEFAULT_CUSTOMER = "test-customer";

    private final EventService eventService;
    private final EventCategoryService eventCategoryService;
    private final EventOrganizerService eventOrganizerService;
    private final CustomerService customerService;
    private final EventBookingService eventBookingService;

    public EventmanagementApplicationRunner(EventService eventService, EventCategoryService eventCategoryService, EventOrganizerService eventOrganizerService, CustomerService customerService, EventBookingService eventBookingService) {
        this.eventService = eventService;
        this.eventCategoryService = eventCategoryService;
        this.eventOrganizerService = eventOrganizerService;
        this.customerService = customerService;
        this.eventBookingService = eventBookingService;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) {
//        eventService.addEvent(new Event(
//                null,
//                "Event 1",
//                "This is the first event ever!",
//                "Dornbirn, Vorarlberg",
//                LocalDateTime.of(2027,1,10,17,0),
//                100f,
//                20,
//                EventStatus.OPEN,
//                "Hiking",
//                null,
//                null
//        ));

        eventOrganizerService.addOrganizer(new EventOrganizerForm(
                //Currently hardcoded default organizer!!!
                DEFAULT_ORGANIZER,
                "example address",
                "example@test.com"
        ));

        customerService.addCustomer(CustomerForm.builder()
                //Currently hardcoded default customer!!!
                .name(DEFAULT_CUSTOMER)
                .address("example address")
                .email("customer@test.com")
                .customerType(CustomerType.PERSON)
                .build());

        eventCategoryService.addCategory(new EventCategoryForm(
                "Hiking"
        ));

        eventService.addEvent(EventForm.builder()
                .name("Event1")
                .location("Dornbirn, Vorarlberg")
                .eventDates(List.of(EventDateForm.builder()
                        .startTime(LocalDateTime.now().plusMonths(1))
                        .endTime(LocalDateTime.now().plusMonths(5))
                        .build()
                ))
                .price(BigDecimal.valueOf(150))
                .minParticipants(3)
                .maxParticipants(50)
                .eventCategoryId(eventCategoryService
                        .getAllEventCategories().getFirst().id())
                .build());

        // demo booking to test if customer gets notified about event changes
        // remove once customers can book events
        EventDto event = eventService.getAllEvents().getFirst();
        eventBookingService.book(event.id(), EventBookingForm.builder()
                .eventDateId(event.eventDates().getFirst().id())
                .eventBookingType(EventBookingType.INDIVIDUAL)
                .paymentMethod(PaymentMethod.CARD)
                .build());
    }
}
