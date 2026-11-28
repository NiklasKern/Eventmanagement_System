package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.application.dto.InvoiceDto;
import at.fhv.eventmangement.application.dto.InvoiceReviewDto;
import at.fhv.eventmangement.application.form.EventBookingForm;
import at.fhv.eventmangement.application.form.InvoiceForm;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotEditableException;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotFoundException;

import java.util.UUID;

public interface InvoiceService {
    InvoiceDto previewInvoice(UUID eventId, EventBookingForm form, String username)
            throws EventNotFoundException, CustomerNotFoundException;

    InvoiceDto getInvoice(UUID id, String username) throws InvoiceNotFoundException;

    InvoiceDto getInvoiceForBooking(UUID bookingId) throws InvoiceNotFoundException;

    InvoiceReviewDto getInvoiceReview(UUID id, String username) throws InvoiceNotFoundException;

    InvoiceDto requestChanges(UUID id, InvoiceForm form, String username) throws InvoiceNotFoundException, InvoiceNotEditableException;

    InvoiceDto confirm(UUID id, String username) throws InvoiceNotFoundException, InvoiceNotEditableException;

    InvoiceDto confirm(UUID id, InvoiceForm form, String username) throws InvoiceNotFoundException, InvoiceNotEditableException;
}
