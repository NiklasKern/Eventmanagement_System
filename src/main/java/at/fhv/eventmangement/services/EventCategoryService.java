package at.fhv.eventmangement.services;

import at.fhv.eventmangement.domain.entities.EventCategory;
import jdk.jfr.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventCategoryService {
    List<EventCategory> getAllEventCategories();
    EventCategory addCategory(String name);
}
