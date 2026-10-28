package at.fhv.eventmangement.services;

import at.fhv.eventmangement.domain.form.EventCategoryForm;
import at.fhv.eventmangement.domain.entities.EventCategory;

import java.util.List;

public interface EventCategoryService {
    List<EventCategory> getAllEventCategories();
    void addCategory(EventCategoryForm form);
}
