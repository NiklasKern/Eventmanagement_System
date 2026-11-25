package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.dto.InvoiceDto;
import at.fhv.eventmangement.application.dto.InvoiceReviewDto;
import at.fhv.eventmangement.application.form.EventBookingForm;
import at.fhv.eventmangement.application.form.InvoiceForm;
import at.fhv.eventmangement.application.mappers.EventBookingMapper;
import at.fhv.eventmangement.application.mappers.EventMapper;
import at.fhv.eventmangement.application.mappers.InvoiceMapper;
import at.fhv.eventmangement.application.services.InvoiceService;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotEditableException;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotFoundException;
import at.fhv.eventmangement.domain.model.*;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import at.fhv.eventmangement.domain.repositories.EventRepository;
import at.fhv.eventmangement.domain.repositories.InvoiceRepository;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final EventRepository eventRepository;
    private final EventBookingRepository eventBookingRepository;
    private final UserAccountRepository userAccountRepository;
    private final InvoiceMapper invoiceMapper;
    private final EventBookingMapper eventBookingMapper;
    private final EventMapper eventMapper;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, EventRepository eventRepository,
                              EventBookingRepository eventBookingRepository, UserAccountRepository userAccountRepository,
                              InvoiceMapper invoiceMapper, EventBookingMapper eventBookingMapper, EventMapper eventMapper) {
        this.invoiceRepository = invoiceRepository;
        this.eventRepository = eventRepository;
        this.eventBookingRepository = eventBookingRepository;
        this.userAccountRepository = userAccountRepository;
        this.invoiceMapper = invoiceMapper;
        this.eventBookingMapper = eventBookingMapper;
        this.eventMapper = eventMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDto previewInvoice(UUID eventId, EventBookingForm form, String username)
            throws EventNotFoundException, CustomerNotFoundException {

        Event event = eventRepository.findById(eventId).orElseThrow(EventNotFoundException::new);
        Customer customer = currentCustomer(username);

        int places = form.getEventBookingType() == EventBookingType.GROUP
                && form.getPlaces() != null && form.getPlaces() > 0 ? form.getPlaces() : 1;

        EventDate date = event.getEventDates().stream()
                .filter(d -> d.getId() != null && d.getId().equals(form.getEventDateId()))
                .findFirst()
                .orElseGet(() -> EventDate.builder().build());
        date.setEvent(event);

        EventBooking booking = EventBooking.builder()
                .customer(customer)
                .eventDate(date)
                .eventBookingType(form.getEventBookingType())
                .places(places)
                .paymentMethod(form.getPaymentMethod())
                .build();

        return invoiceMapper.toDto(Invoice.createInterimFor(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDto getInvoice(UUID id) throws InvoiceNotFoundException {
        return invoiceMapper.toDto(findInvoice(id));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceForBooking(UUID bookingId) throws InvoiceNotFoundException {
        return invoiceMapper.toDto(invoiceRepository.findByBookingId(bookingId)
                .orElseThrow(InvoiceNotFoundException::new));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceReviewDto getInvoiceReview(UUID id) throws InvoiceNotFoundException {
        Invoice invoice = findInvoice(id);
        EventBooking booking = eventBookingRepository.findById(invoice.getBookingId())
                .orElseThrow(InvoiceNotFoundException::new);
        return new InvoiceReviewDto(
                invoiceMapper.toDto(invoice),
                eventBookingMapper.toDto(booking),
                eventMapper.toDto(booking.getEventDate().getEvent()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceDto requestChanges(UUID id, InvoiceForm form)
            throws InvoiceNotFoundException, InvoiceNotEditableException {
        Invoice invoice = findInvoice(id);
        applyChanges(invoice, form);
        return invoiceMapper.toDto(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceDto confirm(UUID id) throws InvoiceNotFoundException, InvoiceNotEditableException {
        Invoice invoice = findInvoice(id);
        invoice.confirm();
        return invoiceMapper.toDto(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceDto confirm(UUID id, InvoiceForm form) throws InvoiceNotFoundException, InvoiceNotEditableException {
        Invoice invoice = findInvoice(id);
        applyChanges(invoice, form);
        invoice.confirm();
        return invoiceMapper.toDto(invoiceRepository.save(invoice));
    }

    private void applyChanges(Invoice invoice, InvoiceForm form) throws InvoiceNotEditableException {
        invoice.requestChanges(
                ContactInfo.builder()
                        .name(form.getName().trim())
                        .address(form.getAddress().trim())
                        .email(form.getEmail().trim())
                        .build(),
                form.getPaymentMethod(),
                invoiceMapper.toItems(form.getItems()));
    }

    // The invoice is billed to the currently logged in customer
    private Customer currentCustomer(String username) throws CustomerNotFoundException {
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("No account for logged-in user " + username));
        if (account.getCustomer() == null) {
            throw new CustomerNotFoundException("Your account is not linked to a customer");
        }
        return account.getCustomer();
    }

    private Invoice findInvoice(UUID id) throws InvoiceNotFoundException {
        return invoiceRepository.findById(id).orElseThrow(InvoiceNotFoundException::new);
    }
}