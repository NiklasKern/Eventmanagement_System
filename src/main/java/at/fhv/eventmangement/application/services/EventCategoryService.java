package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.application.dto.EventCategoryDto;
import at.fhv.eventmangement.application.form.EventCategoryForm;

import java.util.List;

public interface EventCategoryService {
    List<EventCategoryDto> getAllEventCategories();
    void addCategory(EventCategoryForm form);
}
