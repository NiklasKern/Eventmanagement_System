package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.dto.EventOrganizerDto;
import at.fhv.eventmangement.application.form.EventOrganizerForm;
import at.fhv.eventmangement.application.mappers.EventOrganizerMapper;
import at.fhv.eventmangement.application.services.EventOrganizerService;
import at.fhv.eventmangement.domain.exceptions.EventOrganizerAlreadyExistsException;
import at.fhv.eventmangement.domain.model.ContactInfo;
import at.fhv.eventmangement.domain.model.EventOrganizer;
import at.fhv.eventmangement.domain.repositories.EventOrganizerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EventOrganizerServiceImpl implements EventOrganizerService {

    private final EventOrganizerRepository eventOrganizerRepository;
    private final EventOrganizerMapper eventOrganizerMapper;

    public EventOrganizerServiceImpl(EventOrganizerRepository eventOrganizerRepository,
                                     EventOrganizerMapper eventOrganizerMapper) {
        this.eventOrganizerRepository = eventOrganizerRepository;
        this.eventOrganizerMapper = eventOrganizerMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrganizer(EventOrganizerForm form) throws EventOrganizerAlreadyExistsException {

        String name = form.getOrganizerName().trim();

        if (eventOrganizerRepository.existsByContactInfoNameIgnoreCase(name)) {
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

    @Override
    public List<EventOrganizerDto> getAllOrganizers() {
        return eventOrganizerRepository.findAll().stream().map(eventOrganizerMapper::toDto).toList();
    }
}