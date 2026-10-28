package at.fhv.eventmangement;

import at.fhv.eventmangement.domain.dto.EventForm;
import at.fhv.eventmangement.repositories.EventCategoryRepository;
import at.fhv.eventmangement.repositories.EventRepository;
import at.fhv.eventmangement.services.EventCategoryService;
import at.fhv.eventmangement.services.EventService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("local")
public class EventmanagementApplicationRunner implements ApplicationRunner {

    private final EventService eventService;
    private final EventCategoryService eventCategoryService;

    public EventmanagementApplicationRunner(EventService eventService, EventCategoryService eventCategoryService) {
        this.eventService = eventService;
        this.eventCategoryService = eventCategoryService;
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


        eventCategoryService.addCategory("Hiking");

        eventService.addEvent(EventForm.builder()
                .name("Event1")
                .location("Dornbirn, Vorarlberg")
                .dateTime(LocalDateTime.now().plusMonths(1))
                .price(150f)
                .capacity(50)
                .eventCategoryId(eventCategoryService
                        .getAllEventCategories().getFirst().getId()).build());
    }
}
