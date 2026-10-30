package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.application.dto.EventBookingDto;
import at.fhv.eventmangement.application.form.EventBookingForm;

import java.util.UUID;

public interface EventBookingService {
    EventBookingDto book(UUID id, EventBookingForm form);
}
