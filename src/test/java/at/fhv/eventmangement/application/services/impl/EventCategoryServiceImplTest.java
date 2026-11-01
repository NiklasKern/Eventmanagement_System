package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.dto.EventCategoryDto;
import at.fhv.eventmangement.application.exceptions.InvalidInputException;
import at.fhv.eventmangement.application.form.EventCategoryForm;
import at.fhv.eventmangement.application.mappers.EventCategoryMapper;
import at.fhv.eventmangement.domain.model.EventCategory;
import at.fhv.eventmangement.domain.repositories.EventCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventCategoryServiceImplTest {

    @Mock
    private EventCategoryRepository eventCategoryRepository;
    @Mock
    private EventCategoryMapper eventCategoryMapper;

    @InjectMocks
    private EventCategoryServiceImpl eventCategoryService;

    @Test
    void getAllEventCategories_mapsEveryCategory() {
        LocalDateTime now = LocalDateTime.now();
        EventCategory hiking = new EventCategory(UUID.randomUUID(), "Hiking", now, now);
        EventCategoryDto hikingDto = new EventCategoryDto(hiking.getId(), "Hiking");
        when(eventCategoryRepository.findAll()).thenReturn(List.of(hiking));
        when(eventCategoryMapper.toDto(hiking)).thenReturn(hikingDto);

        assertThat(eventCategoryService.getAllEventCategories()).containsExactly(hikingDto);
    }

    @Test
    void addCategory_savesTrimmedNameWithTimestamps() {
        when(eventCategoryRepository.existsByCategoryNameIgnoreCase("Climbing")).thenReturn(false);

        eventCategoryService.addCategory(new EventCategoryForm("  Climbing  "));

        ArgumentCaptor<EventCategory> captor = ArgumentCaptor.forClass(EventCategory.class);
        verify(eventCategoryRepository).save(captor.capture());
        EventCategory saved = captor.getValue();
        assertThat(saved.getId()).isNull();
        assertThat(saved.getCategoryName()).isEqualTo("Climbing");
        assertThat(saved.getCreated()).isNotNull();
        assertThat(saved.getUpdated()).isEqualTo(saved.getCreated());
    }

    @Test
    void addCategory_rejectsDuplicateName() {
        when(eventCategoryRepository.existsByCategoryNameIgnoreCase("Hiking")).thenReturn(true);

        assertThatThrownBy(() -> eventCategoryService.addCategory(new EventCategoryForm("Hiking")))
                .isInstanceOf(InvalidInputException.class)
                .satisfies(ex -> assertThat(((InvalidInputException) ex).getFieldErrors())
                        .containsEntry("categoryName", "category already exists"));
        verify(eventCategoryRepository, never()).save(any());
    }

    @Test
    void addCategory_checksDuplicatesWithTheTrimmedName() {
        // "Hiking " must be recognised as the existing "Hiking"
        when(eventCategoryRepository.existsByCategoryNameIgnoreCase("Hiking")).thenReturn(true);

        assertThatThrownBy(() -> eventCategoryService.addCategory(new EventCategoryForm("Hiking ")))
                .isInstanceOf(InvalidInputException.class);
        verify(eventCategoryRepository, never()).save(any());
    }
}
