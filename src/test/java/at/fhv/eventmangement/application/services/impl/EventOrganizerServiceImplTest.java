package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.exceptions.InvalidInputException;
import at.fhv.eventmangement.application.form.EventOrganizerForm;
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
    void addOrganizer_savesTrimmedNameAndContactData() {
        eventOrganizerService.addOrganizer(
                new EventOrganizerForm("  Alpine Club  ", "Hauptstrasse 1", "info@alpine.at"));

        ArgumentCaptor<EventOrganizer> captor = ArgumentCaptor.forClass(EventOrganizer.class);
        verify(eventOrganizerRepository).save(captor.capture());
        EventOrganizer saved = captor.getValue();
        assertThat(saved.getId()).isNull();
        assertThat(saved.getOrganizerName()).isEqualTo("Alpine Club");
        assertThat(saved.getAddress()).isEqualTo("Hauptstrasse 1");
        assertThat(saved.getEmail()).isEqualTo("info@alpine.at");
        assertThat(saved.getCreated()).isNotNull();
        assertThat(saved.getUpdated()).isEqualTo(saved.getCreated());
    }

    @Test
    void addOrganizer_rejectsDuplicateName() {
        when(eventOrganizerRepository.existsByOrganizerNameIgnoreCase("Alpine Club")).thenReturn(true);

        assertThatThrownBy(() -> eventOrganizerService.addOrganizer(
                new EventOrganizerForm("Alpine Club", "Hauptstrasse 1", "info@alpine.at")))
                .isInstanceOf(InvalidInputException.class)
                .satisfies(ex -> assertThat(((InvalidInputException) ex).getFieldErrors())
                        .containsEntry("organizerName", "organizer name already exists"));
        verify(eventOrganizerRepository, never()).save(any());
    }
}
