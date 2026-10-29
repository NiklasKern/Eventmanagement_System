package at.fhv.eventmangement;

import at.fhv.eventmangement.domain.form.EventCategoryForm;
import at.fhv.eventmangement.domain.form.EventOrganizerForm;
import at.fhv.eventmangement.domain.form.EventForm;
import at.fhv.eventmangement.services.EventCategoryService;
import at.fhv.eventmangement.services.EventOrganizerService;
import at.fhv.eventmangement.services.EventService;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Profile("local")
public class EventmanagementApplicationRunner implements ApplicationRunner {

    // Until there is a login, every event belongs to this organizer
    public static final String DEFAULT_ORGANIZER = "test-organizer";

    private final EventService eventService;
    private final EventCategoryService eventCategoryService;
    private final EventOrganizerService eventOrganizerService;

    public EventmanagementApplicationRunner(EventService eventService, EventCategoryService eventCategoryService, EventOrganizerService eventOrganizerService) {
        this.eventService = eventService;
        this.eventCategoryService = eventCategoryService;
        this.eventOrganizerService = eventOrganizerService;
    }

    @Override
    public void run(@NonNull ApplicationArguments args){
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


        eventCategoryService.addCategory(new EventCategoryForm(
                "Hiking"
        ));

        eventService.addEvent(EventForm.builder()
                .name("Event1")
                .location("Dornbirn, Vorarlberg")
                .dateTime(LocalDateTime.now().plusMonths(1))
                .price(BigDecimal.valueOf(150))
                .minParticipants(3)
                .maxParticipants(50)
                .eventCategoryId(eventCategoryService
                        .getAllEventCategories().getFirst().getId())
                .build());
    }
}
