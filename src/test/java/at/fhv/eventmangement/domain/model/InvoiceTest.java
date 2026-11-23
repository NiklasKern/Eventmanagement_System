package at.fhv.eventmangement.domain.model;

import org.junit.jupiter.api.Test;

import at.fhv.eventmangement.domain.exceptions.InvoiceNotEditableException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceTest {

    @Test
    void item_calculatesNetVatAndGross() {
        InvoiceItem item = InvoiceItem.withStandardVat("Hike", 3, new BigDecimal("33.33"));

        assertThat(item.getVatRate()).isEqualTo(InvoiceItem.STANDARD_VAT_RATE);
        assertThat(item.getNetAmount()).isEqualByComparingTo("99.99");
        assertThat(item.getVatAmount()).isEqualByComparingTo("20.00");
        assertThat(item.getGrossAmount()).isEqualByComparingTo("119.99");
    }

    @Test
    void createInterim_sumsItemsAndCalculatesAdvancePayment() {
        Invoice invoice = Invoice.createInterim(PaymentMethod.CARD, List.of(
                InvoiceItem.withStandardVat("Hike", 2, new BigDecimal("100.00")),
                new InvoiceItem().setDescription("Book").setQuantity(1).setUnitPrice(new BigDecimal("50.00")).setVatRate(10)));

        assertThat(invoice.getInvoiceStatus()).isEqualTo(InvoiceStatus.INTERM);
        assertThat(invoice.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(invoice.getInvoiceDate()).isNotNull();
        assertThat(invoice.getNetTotal()).isEqualByComparingTo("250.00");
        assertThat(invoice.getVatTotal()).isEqualByComparingTo("45.00");
        assertThat(invoice.getGrossTotal()).isEqualByComparingTo("295.00");
        assertThat(invoice.getAdvancePayment()).isEqualByComparingTo("88.50");
        assertThat(invoice.getRemainingAmount()).isEqualByComparingTo("206.50");
    }

    @Test
    void emptyInvoice_hasZeroTotals() {
        Invoice invoice = new Invoice();

        assertThat(invoice.getGrossTotal()).isEqualByComparingTo("0");
        assertThat(invoice.getRemainingAmount()).isEqualByComparingTo("0");
    }

    @Test
    void items_areNotModifiableFromOutside() {
        Invoice invoice = Invoice.createInterim(PaymentMethod.CARD, List.of());

        assertThatThrownBy(() -> invoice.getItems().add(new InvoiceItem()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private EventBooking booking(int places) {
        Event event = Event.builder().name("Summer Hike").price(new BigDecimal("150.00")).build();
        EventDate date = EventDate.builder()
                .event(event)
                .startTime(LocalDateTime.of(2027, 5, 1, 9, 0))
                .endTime(LocalDateTime.of(2027, 5, 1, 17, 0))
                .build();
        return EventBooking.builder()
                .id(UUID.randomUUID())
                .customer(Customer.builder().contactInfo(new ContactInfo("Anna", "Street 1", "anna@example.com")).build())
                .eventDate(date)
                .places(places)
                .paymentMethod(PaymentMethod.CARD)
                .build();
    }

    private InvoiceItem item(String price) {
        return InvoiceItem.withStandardVat("Hike", 1, new BigDecimal(price));
    }

    @Test
    void createInterimFor_billsBookingCustomerForBookedPlaces() {
        EventBooking booking = booking(3);

        Invoice invoice = Invoice.createInterimFor(booking);

        assertThat(invoice.getBookingId()).isEqualTo(booking.getId());
        assertThat(invoice.getContactInfo().getName()).isEqualTo("Anna");
        assertThat(invoice.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(invoice.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getDescription()).isEqualTo("Summer Hike (01.05.2027, 09:00 – 01.05.2027, 17:00)");
            assertThat(item.getQuantity()).isEqualTo(3);
            assertThat(item.getUnitPrice()).isEqualByComparingTo("150.00");
        });
        assertThat(invoice.getGrossTotal()).isEqualByComparingTo("540.00");
        assertThat(invoice.isEditable()).isTrue();
    }

    @Test
    void itemForBooking_withoutDateTimes_usesEventNameOnly() {
        EventBooking booking = booking(1);
        booking.getEventDate().setStartTime(null);

        assertThat(InvoiceItem.forBooking(booking).getDescription()).isEqualTo("Summer Hike");
    }

    @Test
    void requestChanges_updatesAddressPaymentAndItems() throws Exception {
        Invoice invoice = Invoice.createInterim(PaymentMethod.CARD, List.of(item("100.00")));
        ContactInfo newAddress = new ContactInfo("Firma GmbH", "Hauptplatz 1", "office@firma.at");

        invoice.requestChanges(newAddress, PaymentMethod.CARD, List.of(item("200.00"), item("50.00")));

        assertThat(invoice.getContactInfo()).isSameAs(newAddress);
        assertThat(invoice.getItems()).hasSize(2);
        assertThat(invoice.getGrossTotal()).isEqualByComparingTo("300.00");
        assertThat(invoice.getAdvancePayment()).isEqualByComparingTo("90.00");
    }

    @Test
    void requestChanges_withoutItems_keepsItemsAndAdvancePayment() throws Exception {
        Invoice invoice = Invoice.createInterim(PaymentMethod.CARD, List.of(item("100.00")));

        invoice.requestChanges(new ContactInfo("B", "C", "d@e.at"), PaymentMethod.CARD, null);

        assertThat(invoice.getItems()).hasSize(1);
        assertThat(invoice.getAdvancePayment()).isEqualByComparingTo("36.00");
        assertThat(invoice.getContactInfo().getName()).isEqualTo("B");
    }

    @Test
    void confirm_turnsInterimIntoFinalInvoice() throws Exception {
        Invoice invoice = Invoice.createInterim(PaymentMethod.CARD, List.of(item("100.00")));
        invoice.setInvoiceDate(LocalDateTime.of(2020, 1, 1, 0, 0));

        invoice.confirm();

        assertThat(invoice.getInvoiceStatus()).isEqualTo(InvoiceStatus.FINAL);
        assertThat(invoice.isEditable()).isFalse();
        assertThat(invoice.getInvoiceDate()).isAfter(LocalDateTime.of(2020, 1, 1, 0, 0));
    }

    @Test
    void finalInvoice_cannotBeChangedOrConfirmedAgain() throws Exception {
        Invoice invoice = Invoice.createInterim(PaymentMethod.CARD, List.of(item("100.00")));
        invoice.confirm();

        assertThatThrownBy(invoice::confirm).isInstanceOf(InvoiceNotEditableException.class);
        assertThatThrownBy(() -> invoice.requestChanges(new ContactInfo("B", "C", "d@e.at"), PaymentMethod.CARD, null))
                .isInstanceOf(InvoiceNotEditableException.class);
    }

    @Test
    void item_equalityIgnoresIdAndTimestamps() {
        UUID invoiceId = UUID.randomUUID();
        InvoiceItem a = new InvoiceItem(UUID.randomUUID(), invoiceId, "Hike", 2, new BigDecimal("10.00"), 20,
                LocalDateTime.now(), LocalDateTime.now());
        InvoiceItem b = new InvoiceItem(UUID.randomUUID(), invoiceId, "Hike", 2, new BigDecimal("10.00"), 20, null, null);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b).isNotEqualTo(null);
        assertThat(a.getCreated()).isNotNull();
        assertThat(b.setCreated(a.getCreated()).setUpdated(a.getUpdated()).getUpdated()).isEqualTo(a.getUpdated());
        assertThat(a).isNotEqualTo(b.setQuantity(3));
    }

    @Test
    void invoice_equalityIgnoresIdAndTimestamps() {
        UUID bookingId = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.of(2027, 1, 1, 10, 0);
        ContactInfo contact = new ContactInfo("Anna", "Street 1", "anna@example.com");
        Invoice a = new Invoice(UUID.randomUUID(), bookingId, date, contact, InvoiceStatus.INTERM, PaymentMethod.CARD,
                new BigDecimal("30.00"), LocalDateTime.now(), LocalDateTime.now());
        Invoice b = new Invoice(UUID.randomUUID(), bookingId, date, contact, InvoiceStatus.INTERM, PaymentMethod.CARD,
                new BigDecimal("30.00"), null, null);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b).isNotEqualTo(null);
        assertThat(b.setCreated(a.getCreated()).setUpdated(a.getUpdated()).getCreated()).isEqualTo(a.getCreated());
        assertThat(a).isNotEqualTo(b.setInvoiceStatus(InvoiceStatus.FINAL));
    }
}
