package at.fhv.eventmangement;

import at.fhv.eventmangement.application.dto.EventDto;
import at.fhv.eventmangement.application.form.*;
import at.fhv.eventmangement.application.services.*;
import at.fhv.eventmangement.domain.model.CustomerType;
import at.fhv.eventmangement.domain.model.EventBookingType;
import at.fhv.eventmangement.domain.model.PaymentMethod;
import at.fhv.eventmangement.domain.model.Role;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Demo data for local development (profile "local" only, never in production).
// Runs after SecurityDataInitializer (@Order(0)), so the built-in roles already exist.
@Component
@Profile("local")
@Order(1)
public class EventmanagementApplicationRunner implements ApplicationRunner {

    // Names of the demo organizer and customer
    public static final String DEFAULT_ORGANIZER = "test-organizer";
    public static final String DEFAULT_CUSTOMER = "test-customer";

    // Demo logins, linked to the demo organizer and customer
    public static final String DEMO_PROVIDER = "provider";
    public static final String DEMO_CUSTOMER = "customer";
    private static final String DEMO_PROVIDER_PASSWORD = "Provider#2026";
    private static final String DEMO_CUSTOMER_PASSWORD = "Customer#2026";

    private final EventService eventService;
    private final EventCategoryService eventCategoryService;
    private final EventOrganizerService eventOrganizerService;
    private final CustomerService customerService;
    private final EventBookingService eventBookingService;
    private final RoleService roleService;
    private final UserAccountService userAccountService;

    public EventmanagementApplicationRunner(EventService eventService,
                                            EventCategoryService eventCategoryService,
                                            EventOrganizerService eventOrganizerService,
                                            CustomerService customerService,
                                            EventBookingService eventBookingService,
                                            RoleService roleService,
                                            UserAccountService userAccountService) {
        this.eventService = eventService;
        this.eventCategoryService = eventCategoryService;
        this.eventOrganizerService = eventOrganizerService;
        this.customerService = customerService;
        this.eventBookingService = eventBookingService;
        this.roleService = roleService;
        this.userAccountService = userAccountService;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        eventOrganizerService.addOrganizer(new EventOrganizerForm(
                DEFAULT_ORGANIZER,
                "example address",
                "example@test.com"
        ));

        customerService.addCustomer(CustomerForm.builder()
                .name(DEFAULT_CUSTOMER)
                .address("example address")
                .email("customer@test.com")
                .customerType(CustomerType.PERSON)
                .build());

        // Login "provider" → creates events for test-organizer
        userAccountService.createAccount(UserAccountForm.builder()
                .username(DEMO_PROVIDER)
                .password(DEMO_PROVIDER_PASSWORD)
                .confirmPassword(DEMO_PROVIDER_PASSWORD)
                .roleId(roleId(Role.EVENT_PROVIDER))
                .organizerId(organizerId(DEFAULT_ORGANIZER))
                .build());

        // Login "customer" → books for test-customer
        userAccountService.createAccount(UserAccountForm.builder()
                .username(DEMO_CUSTOMER)
                .password(DEMO_CUSTOMER_PASSWORD)
                .confirmPassword(DEMO_CUSTOMER_PASSWORD)
                .roleId(roleId(Role.CUSTOMER))
                .customerId(customerId(DEFAULT_CUSTOMER))
                .build());

        eventCategoryService.addCategory(new EventCategoryForm(
                "Hiking"
        ));

        eventService.addEvent(EventForm.builder()
                .name("Event1")
                .location("Dornbirn, Vorarlberg")
                .eventDates(List.of(EventDateForm.builder()
                        .startTime(LocalDateTime.now().plusMonths(1))
                        .endTime(LocalDateTime.now().plusMonths(5))
                        .build()
                ))
                .price(BigDecimal.valueOf(150))
                .minParticipants(3)
                .maxParticipants(50)
                .eventCategoryId(eventCategoryService
                        .getAllEventCategories().getFirst().id())
                .build(), DEMO_PROVIDER);

        // Demo booking, so a customer gets notified when the event is changed
        EventDto event = eventService.getAllEvents().getFirst();
        eventBookingService.book(event.id(), EventBookingForm.builder()
                .eventDateId(event.eventDates().getFirst().id())
                .eventBookingType(EventBookingType.INDIVIDUAL)
                .paymentMethod(PaymentMethod.CARD)
                .build(), DEMO_CUSTOMER);
    }

    private UUID roleId(String roleName) {
        return roleService.getAllRoles().stream()
                .filter(role -> role.name().equals(roleName))
                .findFirst().orElseThrow().id();
    }

    private UUID organizerId(String name) {
        return eventOrganizerService.getAllOrganizers().stream()
                .filter(organizer -> organizer.contactInfo().name().equals(name))
                .findFirst().orElseThrow().id();
    }

    private UUID customerId(String name) {
        return customerService.getAllCustomers().stream()
                .filter(customer -> customer.contactInfo().name().equals(name))
                .findFirst().orElseThrow().id();
    }
}