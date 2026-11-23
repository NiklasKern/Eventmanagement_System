package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.TestcontainersConfiguration;
import at.fhv.eventmangement.domain.model.ContactInfo;
import at.fhv.eventmangement.domain.model.Invoice;
import at.fhv.eventmangement.domain.model.InvoiceItem;
import at.fhv.eventmangement.domain.model.InvoiceStatus;
import at.fhv.eventmangement.domain.model.PaymentMethod;
import at.fhv.eventmangement.domain.repositories.InvoiceRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class InvoiceRepositoryAdapterIntegrationTest {

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private EntityManager entityManager;

    private Invoice newInvoice(UUID bookingId) {
        return Invoice.createInterim(PaymentMethod.CARD, List.of(
                        InvoiceItem.withStandardVat("Summer Hike", 2, new BigDecimal("150.00"))))
                .setBookingId(bookingId)
                .setContactInfo(new ContactInfo("Anna", "Street 1", "anna@example.com"));
    }

    private Invoice reload(UUID id) {
        entityManager.flush();
        entityManager.clear();
        return invoiceRepository.findById(id).orElseThrow();
    }

    @Test
    void save_persistsInvoiceWithItems() {
        UUID bookingId = UUID.randomUUID();
        Invoice saved = invoiceRepository.save(newInvoice(bookingId));

        Invoice found = reload(saved.getId());

        assertThat(found.getBookingId()).isEqualTo(bookingId);
        assertThat(found.getInvoiceStatus()).isEqualTo(InvoiceStatus.INTERM);
        assertThat(found.getContactInfo().getEmail()).isEqualTo("anna@example.com");
        assertThat(found.getAdvancePayment()).isEqualByComparingTo("108.00");
        assertThat(found.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isNotNull();
            assertThat(item.getInvoiceId()).isEqualTo(saved.getId());
            assertThat(item.getQuantity()).isEqualTo(2);
        });
        assertThat(found.getGrossTotal()).isEqualByComparingTo("360.00");
    }

    @Test
    void findByBookingId_findsInvoice() {
        UUID bookingId = UUID.randomUUID();
        Invoice saved = invoiceRepository.save(newInvoice(bookingId));
        entityManager.flush();
        entityManager.clear();

        assertThat(invoiceRepository.findByBookingId(bookingId)).get()
                .extracting(Invoice::getId).isEqualTo(saved.getId());
        assertThat(invoiceRepository.findByBookingId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void requestChanges_replacedItemsAreRemovedFromDb() throws Exception {
        Invoice saved = invoiceRepository.save(newInvoice(UUID.randomUUID()));
        Invoice invoice = reload(saved.getId());

        invoice.requestChanges(new ContactInfo("Firma GmbH", "Hauptplatz 1", "office@firma.at"), PaymentMethod.CARD,
                List.of(InvoiceItem.withStandardVat("Hike", 1, new BigDecimal("100.00")),
                        InvoiceItem.withStandardVat("Lunch", 2, new BigDecimal("20.00"))));
        invoiceRepository.save(invoice);
        Invoice found = reload(saved.getId());

        assertThat(found.getContactInfo().getName()).isEqualTo("Firma GmbH");
        assertThat(found.getItems()).extracting(InvoiceItem::getDescription).containsExactlyInAnyOrder("Hike", "Lunch");
        assertThat(found.getGrossTotal()).isEqualByComparingTo("168.00");
        Long itemRows = entityManager.createQuery(
                        "select count(i) from InvoiceItemEntity i where i.invoice.id = :id", Long.class)
                .setParameter("id", saved.getId()).getSingleResult();
        assertThat(itemRows).isEqualTo(2);
    }

    @Test
    void confirm_persistsFinalStatus() throws Exception {
        Invoice saved = invoiceRepository.save(newInvoice(UUID.randomUUID()));
        Invoice invoice = reload(saved.getId());

        invoice.confirm();
        invoiceRepository.save(invoice);

        assertThat(reload(saved.getId()).getInvoiceStatus()).isEqualTo(InvoiceStatus.FINAL);
    }
}
