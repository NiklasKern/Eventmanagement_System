package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.form.EventOrganizerForm;
import at.fhv.eventmangement.domain.model.EventOrganizer;
import at.fhv.eventmangement.domain.exceptions.ValidationException;
import at.fhv.eventmangement.domain.repositories.EventOrganizerRepository;
import at.fhv.eventmangement.application.services.EventOrganizerService;
import jakarta.transaction.Transactional;
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
    @Transactional
    public void addOrganizer(EventOrganizerForm form) {

        if(eventOrganizerRepository.existsByOrganizerNameIgnoreCase(form.getOrganizerName()))
        {
            throw new ValidationException(Map.of("organizerName","organizer name already exists"));
        }

        LocalDateTime now = LocalDateTime.now();

        eventOrganizerRepository.save(new EventOrganizer(
                null,
                form.getOrganizerName().trim(),
                form.getAddress(),
                form.getEmail(),
                now,
                now
        ));
    }
}
