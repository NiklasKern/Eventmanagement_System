package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.domain.exceptions.EventOrganizerAlreadyExistsException;
import at.fhv.eventmangement.application.form.EventOrganizerForm;
import at.fhv.eventmangement.domain.model.ContactInfo;
import at.fhv.eventmangement.domain.model.EventOrganizer;
import at.fhv.eventmangement.domain.repositories.EventOrganizerRepository;
import at.fhv.eventmangement.application.services.EventOrganizerService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional(readOnly = true)
public class EventOrganizerServiceImpl implements EventOrganizerService {

    private final EventOrganizerRepository eventOrganizerRepository;

    public EventOrganizerServiceImpl(EventOrganizerRepository eventOrganizerRepository) {
        this.eventOrganizerRepository = eventOrganizerRepository;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrganizer(EventOrganizerForm form) throws EventOrganizerAlreadyExistsException {

        String name = form.getOrganizerName().trim();

        if(eventOrganizerRepository.existsByContactInfoNameIgnoreCase(name))
        {
            throw new EventOrganizerAlreadyExistsException();
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
