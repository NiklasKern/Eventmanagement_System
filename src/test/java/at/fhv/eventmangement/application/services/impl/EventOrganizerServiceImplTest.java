package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.domain.exceptions.EventOrganizerAlreadyExistsException;
import at.fhv.eventmangement.application.form.EventOrganizerForm;
import at.fhv.eventmangement.domain.model.ContactInfo;
import at.fhv.eventmangement.domain.model.EventOrganizer;
import at.fhv.eventmangement.domain.repositories.EventOrganizerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventOrganizerServiceImplTest {

    @Mock
    private EventOrganizerRepository eventOrganizerRepository;

    @InjectMocks
    private EventOrganizerServiceImpl eventOrganizerService;

    @Test
    void addOrganizer_savesTrimmedNameAndContactData() throws Exception {
        eventOrganizerService.addOrganizer(
                new EventOrganizerForm("Alpine Club", "Hauptstrasse 1", "info@alpine.at"));

        ArgumentCaptor<EventOrganizer> captor = ArgumentCaptor.forClass(EventOrganizer.class);
        verify(eventOrganizerRepository).save(captor.capture());
        EventOrganizer saved = captor.getValue();
        assertThat(saved.getId()).isNull();
        assertThat(saved.getContactInfo()).usingRecursiveComparison()
                .isEqualTo(new ContactInfo("Alpine Club", "Hauptstrasse 1", "info@alpine.at"));
        // created/updated are set by the database (@CreationTimestamp/@UpdateTimestamp)
    }

    @Test
    void addOrganizer_rejectsDuplicateName() throws Exception {
        when(eventOrganizerRepository.existsByContactInfoNameIgnoreCase("Alpine Club")).thenReturn(true);

        assertThatThrownBy(() -> eventOrganizerService.addOrganizer(
                new EventOrganizerForm("Alpine Club", "Hauptstrasse 1", "info@alpine.at")))
                .isInstanceOf(EventOrganizerAlreadyExistsException.class)
                .hasMessage("organizer name already exists");
        verify(eventOrganizerRepository, never()).save(any());
    }
}
