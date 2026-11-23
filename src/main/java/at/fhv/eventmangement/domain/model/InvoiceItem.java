package at.fhv.eventmangement.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class InvoiceItem {
    public static final int STANDARD_VAT_RATE = 20;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");

    private UUID id;
    private UUID invoiceId;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Integer vatRate;
    private LocalDateTime created;
    private LocalDateTime updated;

    public InvoiceItem() {
    }

    public InvoiceItem(UUID id, UUID invoiceId, String description, Integer quantity, BigDecimal unitPrice, Integer vatRate, LocalDateTime created, LocalDateTime updated) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.vatRate = vatRate;
        this.created = created;
        this.updated = updated;
    }

    public static InvoiceItem forBooking(EventBooking booking) {
        EventDate date = booking.getEventDate();
        Event event = date.getEvent();
        String description = date.getStartTime() == null || date.getEndTime() == null
                ? event.getName()
                : event.getName() + " (" + date.getStartTime().format(DATE_FORMAT)
                        + " – " + date.getEndTime().format(DATE_FORMAT) + ")";
        return withStandardVat(description, booking.getPlaces(), event.getPrice());
    }

    public static InvoiceItem withStandardVat(String description, int quantity, BigDecimal unitPrice) {
        return new InvoiceItem()
                .setDescription(description)
                .setQuantity(quantity)
                .setUnitPrice(unitPrice)
                .setVatRate(STANDARD_VAT_RATE);
    }

    public UUID getId() {
        return id;
    }

    public InvoiceItem setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public InvoiceItem setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public InvoiceItem setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public InvoiceItem setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public InvoiceItem setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        return this;
    }

    public Integer getVatRate() {
        return vatRate;
    }

    public InvoiceItem setVatRate(Integer vatRate) {
        this.vatRate = vatRate;
        return this;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public InvoiceItem setCreated(LocalDateTime created) {
        this.created = created;
        return this;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public InvoiceItem setUpdated(LocalDateTime updated) {
        this.updated = updated;
        return this;
    }

    public BigDecimal getNetAmount() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getVatAmount() {
        return getNetAmount().multiply(BigDecimal.valueOf(vatRate))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getGrossAmount() {
        return getNetAmount().add(getVatAmount());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceItem that = (InvoiceItem) o;
        return Objects.equals(invoiceId, that.invoiceId) && Objects.equals(description, that.description) && Objects.equals(quantity, that.quantity) && Objects.equals(unitPrice, that.unitPrice) && Objects.equals(vatRate, that.vatRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId, description, quantity, unitPrice, vatRate);
    }
}
