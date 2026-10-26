package at.fhv.eventmangement;

import at.fhv.eventmangement.domain.dto.EventForm;
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

    public EventmanagementApplicationRunner(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
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

        eventService.addEvent(new EventForm()
                .setName("Event1")
                .setDateTime(LocalDateTime.now())
                .setPrice(150f)
                .setCapacity(50)
                .setEventCategory("Hiking"));
    }
}
