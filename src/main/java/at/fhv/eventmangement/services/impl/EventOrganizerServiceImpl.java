package at.fhv.eventmangement.services.impl;

import at.fhv.eventmangement.domain.form.EventOrganizerForm;
import at.fhv.eventmangement.domain.entities.EventOrganizer;
import at.fhv.eventmangement.exceptions.ValidationException;
import at.fhv.eventmangement.repositories.EventOrganizerRepository;
import at.fhv.eventmangement.services.EventOrganizerService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class EventOrganizerServiceImpl implements EventOrganizerService {

    private final EventOrganizerRepository eventOrganizerRepository;

    public EventOrganizerServiceImpl(EventOrganizerRepository eventOrganizerRepository) {
        this.eventOrganizerRepository = eventOrganizerRepository;
    }


    @Override
    public EventOrganizer addOrganizer(EventOrganizerForm form) {

        if(eventOrganizerRepository.existsByOrganizerNameIgnoreCase(form.getOrganizerName()))
        {
            throw new ValidationException(Map.of("organizerName","organizer name already exists"));
        }

        LocalDateTime now = LocalDateTime.now();

        return eventOrganizerRepository.save(new EventOrganizer(
                null,
                form.getOrganizerName().trim(),
                form.getAddress(),
                form.getEmail(),
                now,
                now
        ));
    }
}
