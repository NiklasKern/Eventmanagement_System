package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.dto.EventBookingDto;
import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.dto.InvoiceDto;
import at.fhv.eventmangement.application.dto.InvoiceReviewDto;
import at.fhv.eventmangement.application.form.EventBookingForm;
import at.fhv.eventmangement.application.form.InvoiceForm;
import at.fhv.eventmangement.application.form.InvoiceItemForm;
import at.fhv.eventmangement.application.mappers.ContactInfoMapperImpl;
import at.fhv.eventmangement.application.mappers.EventBookingMapper;
import at.fhv.eventmangement.application.mappers.EventMapper;
import at.fhv.eventmangement.application.mappers.InvoiceMapper;
import at.fhv.eventmangement.application.mappers.InvoiceMapperImpl;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.exceptions.EventNotFoundException;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotEditableException;
import at.fhv.eventmangement.domain.exceptions.InvoiceNotFoundException;
import at.fhv.eventmangement.domain.model.*;
import at.fhv.eventmangement.domain.repositories.CustomerRepository;
import at.fhv.eventmangement.domain.repositories.EventBookingRepository;
import at.fhv.eventmangement.domain.repositories.EventRepository;
import at.fhv.eventmangement.domain.repositories.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventBookingRepository eventBookingRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private EventBookingMapper eventBookingMapper;
    @Mock
    private EventMapper eventMapper;

    private InvoiceServiceImpl invoiceService;

    private final UUID eventId = UUID.randomUUID();
    private final UUID dateId = UUID.randomUUID();
    private Event event;

    @BeforeEach
    void setUp() {
        InvoiceMapperImpl invoiceMapper = new InvoiceMapperImpl();
        ReflectionTestUtils.setField(invoiceMapper, "contactInfoMapper", new ContactInfoMapperImpl());
        invoiceService = new InvoiceServiceImpl(invoiceRepository, eventRepository, eventBookingRepository,
                customerRepository, invoiceMapper, eventBookingMapper, eventMapper);

        EventDate date = EventDate.builder()
                .id(dateId)
                .startTime(LocalDateTime.of(2027, 5, 1, 9, 0))
                .endTime(LocalDateTime.of(2027, 5, 1, 17, 0))
                .build();
        event = Event.builder()
                .id(eventId)
                .name("Summer Hike")
                .price(new BigDecimal("150.00"))
                .eventDates(List.of(date))
                .build();
    }

    private void eventAndCustomerExist() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(customerRepository.findByContactInfoName(any())).thenReturn(Optional.of(Customer.builder()
                .contactInfo(new ContactInfo("Anna", "Street 1", "anna@example.com")).build()));
    }

    private Invoice interimInvoice(UUID id) {
        return Invoice.createInterim(PaymentMethod.CARD, List.of(InvoiceItem.withStandardVat("Hike", 2, new BigDecimal("100.00"))))
                .setId(id)
                .setBookingId(UUID.randomUUID())
                .setContactInfo(new ContactInfo("Anna", "Street 1", "anna@example.com"));
    }

    private InvoiceForm changeForm(List<InvoiceItemForm> items) {
        return InvoiceForm.builder()
                .name(" Firma GmbH ").address("Hauptplatz 1").email("office@firma.at")
                .paymentMethod(PaymentMethod.CARD).items(items).build();
    }

    @Test
    void preview_groupBooking_calculatesTotalsForAllPlaces() throws Exception {
        eventAndCustomerExist();
        EventBookingForm form = EventBookingForm.builder()
                .eventDateId(dateId).eventBookingType(EventBookingType.GROUP).places(3)
                .paymentMethod(PaymentMethod.CARD).build();

        InvoiceDto preview = invoiceService.previewInvoice(eventId, form);

        assertThat(preview.id()).isNull();
        assertThat(preview.invoiceStatus()).isEqualTo(InvoiceStatus.INTERM);
        assertThat(preview.paymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(preview.contactInfo().name()).isEqualTo("Anna");
        assertThat(preview.items()).singleElement().satisfies(item -> {
            assertThat(item.quantity()).isEqualTo(3);
            assertThat(item.vatRate()).isEqualTo(20);
            assertThat(item.description()).isEqualTo("Summer Hike (01.05.2027, 09:00 – 01.05.2027, 17:00)");
        });
        assertThat(preview.netTotal()).isEqualByComparingTo("450.00");
        assertThat(preview.vatTotal()).isEqualByComparingTo("90.00");
        assertThat(preview.grossTotal()).isEqualByComparingTo("540.00");
        assertThat(preview.advancePayment()).isEqualByComparingTo("162.00");
        assertThat(preview.remainingAmount()).isEqualByComparingTo("378.00");
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void preview_nonGroupBooking_ignoresPlacesAndWorksWithoutDate() throws Exception {
        eventAndCustomerExist();
        EventBookingForm form = EventBookingForm.builder()
                .eventBookingType(EventBookingType.INDIVIDUAL).places(7).build();

        InvoiceDto preview = invoiceService.previewInvoice(eventId, form);

        assertThat(preview.items()).singleElement().satisfies(item -> {
            assertThat(item.quantity()).isEqualTo(1);
            assertThat(item.description()).isEqualTo("Summer Hike");
        });
        assertThat(preview.grossTotal()).isEqualByComparingTo("180.00");
    }

    @Test
    void preview_unknownEvent_throws() {
        UUID unknown = UUID.randomUUID();
        when(eventRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.previewInvoice(unknown, new EventBookingForm()))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void preview_missingCustomer_throws() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(customerRepository.findByContactInfoName(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.previewInvoice(eventId, new EventBookingForm()))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void getInvoice_returnsDtoWithTotals() throws Exception {
        UUID id = UUID.randomUUID();
        when(invoiceRepository.findById(id)).thenReturn(Optional.of(interimInvoice(id)));

        InvoiceDto dto = invoiceService.getInvoice(id);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.grossTotal()).isEqualByComparingTo("240.00");
        assertThat(dto.editable()).isTrue();
    }

    @Test
    void getInvoice_unknownId_throws() {
        UUID id = UUID.randomUUID();
        when(invoiceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.getInvoice(id)).isInstanceOf(InvoiceNotFoundException.class);
    }

    @Test
    void getInvoiceForBooking_findsByBookingId() throws Exception {
        UUID bookingId = UUID.randomUUID();
        when(invoiceRepository.findByBookingId(bookingId)).thenReturn(Optional.of(interimInvoice(UUID.randomUUID())));

        assertThat(invoiceService.getInvoiceForBooking(bookingId).invoiceStatus()).isEqualTo(InvoiceStatus.INTERM);
    }

    @Test
    void getInvoiceForBooking_missing_throws() {
        UUID bookingId = UUID.randomUUID();
        when(invoiceRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.getInvoiceForBooking(bookingId))
                .isInstanceOf(InvoiceNotFoundException.class);
    }

    @Test
    void getInvoiceReview_containsInvoiceBookingAndEvent() throws Exception {
        UUID id = UUID.randomUUID();
        Invoice invoice = interimInvoice(id);
        EventBooking booking = EventBooking.builder()
                .id(invoice.getBookingId())
                .eventDate(EventDate.builder().event(event).build())
                .build();
        EventBookingDto bookingDto = mock(EventBookingDto.class);
        EventDto eventDto = mock(EventDto.class);
        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));
        when(eventBookingRepository.findById(invoice.getBookingId())).thenReturn(Optional.of(booking));
        when(eventBookingMapper.toDto(booking)).thenReturn(bookingDto);
        when(eventMapper.toDto(event)).thenReturn(eventDto);

        InvoiceReviewDto review = invoiceService.getInvoiceReview(id);

        assertThat(review.invoice().id()).isEqualTo(id);
        assertThat(review.booking()).isSameAs(bookingDto);
        assertThat(review.event()).isSameAs(eventDto);
    }

    @Test
    void getInvoiceReview_bookingMissing_throws() {
        UUID id = UUID.randomUUID();
        Invoice invoice = interimInvoice(id);
        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));
        when(eventBookingRepository.findById(invoice.getBookingId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.getInvoiceReview(id)).isInstanceOf(InvoiceNotFoundException.class);
    }

    @Test
    void requestChanges_withItems_replacesItemsAndSaves() throws Exception {
        UUID id = UUID.randomUUID();
        when(invoiceRepository.findById(id)).thenReturn(Optional.of(interimInvoice(id)));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceDto dto = invoiceService.requestChanges(id, changeForm(List.of(
                new InvoiceItemForm(" Hike ", 1, new BigDecimal("50.00"), 10))));

        ArgumentCaptor<Invoice> saved = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(saved.capture());
        assertThat(saved.getValue().getContactInfo().getName()).isEqualTo("Firma GmbH");
        assertThat(saved.getValue().getItems()).singleElement()
                .satisfies(item -> assertThat(item.getDescription()).isEqualTo("Hike"));
        assertThat(dto.grossTotal()).isEqualByComparingTo("55.00");
        assertThat(dto.advancePayment()).isEqualByComparingTo("16.50");
    }

    @Test
    void requestChanges_withoutItems_keepsItems() throws Exception {
        UUID id = UUID.randomUUID();
        when(invoiceRepository.findById(id)).thenReturn(Optional.of(interimInvoice(id)));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceDto dto = invoiceService.requestChanges(id, changeForm(null));

        assertThat(dto.contactInfo().address()).isEqualTo("Hauptplatz 1");
        assertThat(dto.grossTotal()).isEqualByComparingTo("240.00");
    }

    @Test
    void requestChanges_finalInvoice_throwsAndDoesNotSave() throws Exception {
        UUID id = UUID.randomUUID();
        Invoice invoice = interimInvoice(id);
        invoice.confirm();
        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> invoiceService.requestChanges(id, changeForm(null)))
                .isInstanceOf(InvoiceNotEditableException.class);
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void confirm_savesFinalInvoice() throws Exception {
        UUID id = UUID.randomUUID();
        when(invoiceRepository.findById(id)).thenReturn(Optional.of(interimInvoice(id)));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceDto dto = invoiceService.confirm(id);

        assertThat(dto.invoiceStatus()).isEqualTo(InvoiceStatus.FINAL);
        assertThat(dto.editable()).isFalse();
    }

    @Test
    void confirm_unknownInvoice_throws() {
        UUID id = UUID.randomUUID();
        when(invoiceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.confirm(id)).isInstanceOf(InvoiceNotFoundException.class);
    }

    @Test
    void toForm_prefillsCurrentValues() {
        InvoiceMapper mapper = new InvoiceMapperImpl();
        ReflectionTestUtils.setField(mapper, "contactInfoMapper", new ContactInfoMapperImpl());

        InvoiceForm form = mapper.toForm(mapper.toDto(interimInvoice(UUID.randomUUID())));

        assertThat(form.getName()).isEqualTo("Anna");
        assertThat(form.getEmail()).isEqualTo("anna@example.com");
        assertThat(form.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(form.getItems()).isNull();
    }
}
