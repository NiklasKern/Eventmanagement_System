package at.fhv.eventmangement;

import at.fhv.eventmangement.domain.dto.EventForm;
import at.fhv.eventmangement.domain.entities.Event;
import at.fhv.eventmangement.domain.entities.EventStatus;
import at.fhv.eventmangement.services.EventService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EventmanagementApplicationRunner implements ApplicationRunner {

    private final EventService eventService;

    @Value("${spring.profiles.default}")
    String activeProfile;

    public EventmanagementApplicationRunner(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if(!activeProfile.equals("local")) {return;}

//        eventService.addEvent(new EventForm(
//                null,
//                "Event 1",
//                "This is the first evet ever!",
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
