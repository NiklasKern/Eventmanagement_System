package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.application.dto.EventBookingDto;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventDateNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventNotBookableException;
import at.fhv.eventmangement.domain.exceptions.NotEnoughPlacesException;
import at.fhv.eventmangement.application.form.EventBookingForm;

import java.util.List;
import java.util.UUID;

public interface EventBookingService {
    EventBookingDto book(UUID id, EventBookingForm form)
            throws EventDateNotFoundException, EventNotBookableException, NotEnoughPlacesException, CustomerNotFoundException;
    List<String> getBookingTypes();
    List<String> getPaymentMethods();
}
