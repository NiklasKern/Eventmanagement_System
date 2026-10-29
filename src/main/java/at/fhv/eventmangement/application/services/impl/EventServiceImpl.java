package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.EventmanagementApplicationRunner;
import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.mappers.EventMapper;
import at.fhv.eventmangement.domain.model.Event;
import at.fhv.eventmangement.domain.model.EventCategory;
import at.fhv.eventmangement.domain.model.EventOrganizer;
import at.fhv.eventmangement.domain.model.EventStatus;
import at.fhv.eventmangement.application.form.EventForm;
import at.fhv.eventmangement.domain.exceptions.ValidationException;
import at.fhv.eventmangement.domain.repositories.EventCategoryRepository;
import at.fhv.eventmangement.domain.repositories.EventOrganizerRepository;
import at.fhv.eventmangement.domain.repositories.EventRepository;
import at.fhv.eventmangement.application.services.EventService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Validated
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final EventOrganizerRepository eventOrganizerRepository;

    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository, EventCategoryRepository eventCategoryRepository, EventOrganizerRepository eventOrganizerRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventCategoryRepository = eventCategoryRepository;
        this.eventOrganizerRepository = eventOrganizerRepository;
        this.eventMapper = eventMapper;
    }


    @Override
    public List<EventDto> getEvents() {
        return eventRepository.findAll().stream().map(eventMapper::toDto).toList();
    }

    @Override
    @Transactional
    public EventDto addEvent(EventForm form) {

        if (form.getMinParticipants() > form.getMaxParticipants()) {
            throw new ValidationException(
                    Map.of("maxParticipants", "must not be less than minimum participants"));
        }

        if(eventRepository.existsByName(form.getName())) {
            throw new ValidationException(
                    Map.of("name", "event name already exists"));
        }

        EventCategory category = eventCategoryRepository
                .findById(form.getEventCategoryId())
                .orElseThrow(() -> new ValidationException(
                        Map.of("eventCategoryId", "unknown category")));

        EventOrganizer organizer = eventOrganizerRepository.findByOrganizerName(EventmanagementApplicationRunner.DEFAULT_ORGANIZER).orElseThrow();
        LocalDateTime now = LocalDateTime.now();

        //Create new event to prevent unexpected input
        Event event = Event.builder()
                .name(form.getName())
                .description(Optional.ofNullable(form.getDescription()).orElse(""))
                .location(form.getLocation())
                .price(form.getPrice())
                .minParticipants(form.getMinParticipants())
                .maxParticipants(form.getMaxParticipants())
                .dateTime(form.getDateTime())
                .eventStatus(EventStatus.OPEN)
                .eventCategory(category)
                .eventOrganizer(organizer)
                .created(now)
                .updated(now)
                .build();

        //Save event to db
        eventRepository.save(event);

        //return saved eventdto
        return eventMapper.toDto(event);
    }

}
