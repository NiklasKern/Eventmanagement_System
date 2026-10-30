package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.form.EventOrganizerForm;
import at.fhv.eventmangement.domain.model.ContactInfo;
import at.fhv.eventmangement.domain.model.EventOrganizer;
import at.fhv.eventmangement.domain.exceptions.ValidationException;
import at.fhv.eventmangement.domain.repositories.EventOrganizerRepository;
import at.fhv.eventmangement.application.services.EventOrganizerService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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

        String name = form.getOrganizerName().trim();

        if(eventOrganizerRepository.existsByContactInfoNameIgnoreCase(name))
        {
            throw new ValidationException(Map.of("organizerName","organizer name already exists"));
        }

        eventOrganizerRepository.save(EventOrganizer.builder()
                .contactInfo(ContactInfo.builder()
                        .name(name)
                        .address(form.getAddress().trim())
                        .email(form.getEmail().trim())
                        .build()
                )
                .build()
        );
    }
}
