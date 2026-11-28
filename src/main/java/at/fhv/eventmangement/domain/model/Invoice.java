package at.fhv.eventmangement.domain.model;

import at.fhv.eventmangement.domain.exceptions.InvoiceNotEditableException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

public class Invoice {
    public static final BigDecimal ADVANCE_PAYMENT_RATE = new BigDecimal("0.30");

    private UUID id;
    private UUID bookingId;
    private LocalDateTime invoiceDate;
    private ContactInfo contactInfo;
    private InvoiceStatus invoiceStatus;
    private PaymentMethod paymentMethod;
    private BigDecimal advancePayment;
    private LocalDateTime created;
    private LocalDateTime updated;
    private List<InvoiceItem> items = new ArrayList<>();

    public Invoice() {
    }

    public Invoice(UUID id, UUID bookingId, LocalDateTime invoiceDate, ContactInfo contactInfo, InvoiceStatus invoiceStatus, PaymentMethod paymentMethod, BigDecimal advancePayment, LocalDateTime created, LocalDateTime updated) {
        this.id = id;
        this.bookingId = bookingId;
        this.invoiceDate = invoiceDate;
        this.contactInfo = contactInfo;
        this.invoiceStatus = invoiceStatus;
        this.paymentMethod = paymentMethod;
        this.advancePayment = advancePayment;
        this.created = created;
        this.updated = updated;
    }

    public static Invoice createInterim(PaymentMethod paymentMethod, List<InvoiceItem> items) {
        Invoice invoice = new Invoice()
                .setInvoiceDate(LocalDateTime.now())
                .setInvoiceStatus(InvoiceStatus.INTERM)
                .setPaymentMethod(paymentMethod)
                .setItems(items);
        invoice.recalculateAdvancePayment();
        return invoice;
    }

    public static Invoice createInterimFor(EventBooking booking) {
        return createInterim(booking.getPaymentMethod(), List.of(InvoiceItem.forBooking(booking)))
                .setBookingId(booking.getId())
                .setContactInfo(booking.getCustomer() == null ? null : booking.getCustomer().getContactInfo());
    }

    // no Items list so that customer can only change contact info and payment method
    // Items and prices only come from the booking (createInterimFor)
    public void requestChanges(ContactInfo contactInfo, PaymentMethod paymentMethod)
            throws InvoiceNotEditableException {
        ensureInterim();
        this.contactInfo = contactInfo;
        this.paymentMethod = paymentMethod;
    }

    public void confirm() throws InvoiceNotEditableException {
        ensureInterim();
        this.invoiceStatus = InvoiceStatus.FINAL;
        this.invoiceDate = LocalDateTime.now();
    }

    public boolean isEditable() {
        return invoiceStatus == InvoiceStatus.INTERM;
    }

    private void ensureInterim() throws InvoiceNotEditableException {
        if (!isEditable()) {
            throw new InvoiceNotEditableException();
        }
    }

    private void recalculateAdvancePayment() {
        this.advancePayment = getGrossTotal().multiply(ADVANCE_PAYMENT_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getNetTotal() {
        return sum(InvoiceItem::getNetAmount);
    }

    public BigDecimal getVatTotal() {
        return sum(InvoiceItem::getVatAmount);
    }

    public BigDecimal getGrossTotal() {
        return getNetTotal().add(getVatTotal());
    }

    public BigDecimal getRemainingAmount() {
        return advancePayment == null ? getGrossTotal() : getGrossTotal().subtract(advancePayment);
    }

    private BigDecimal sum(Function<InvoiceItem, BigDecimal> amount) {
        return items.stream().map(amount).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public List<InvoiceItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Invoice setItems(List<InvoiceItem> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
        return this;
    }

    public UUID getId() {
        return id;
    }

    public Invoice setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public Invoice setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
        return this;
    }

    public LocalDateTime getInvoiceDate() {
        return invoiceDate;
    }

    public Invoice setInvoiceDate(LocalDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
        return this;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public Invoice setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
        return this;
    }

    public InvoiceStatus getInvoiceStatus() {
        return invoiceStatus;
    }

    public Invoice setInvoiceStatus(InvoiceStatus invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
        return this;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public Invoice setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public BigDecimal getAdvancePayment() {
        return advancePayment;
    }

    public Invoice setAdvancePayment(BigDecimal advancePayment) {
        this.advancePayment = advancePayment;
        return this;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public Invoice setCreated(LocalDateTime created) {
        this.created = created;
        return this;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public Invoice setUpdated(LocalDateTime updated) {
        this.updated = updated;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(bookingId, invoice.bookingId) && Objects.equals(invoiceDate, invoice.invoiceDate) && Objects.equals(contactInfo, invoice.contactInfo) && invoiceStatus == invoice.invoiceStatus && paymentMethod == invoice.paymentMethod && Objects.equals(advancePayment, invoice.advancePayment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, invoiceDate, contactInfo, invoiceStatus, paymentMethod, advancePayment);
    }

}
