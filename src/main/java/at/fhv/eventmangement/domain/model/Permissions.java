package at.fhv.eventmangement.domain.model;

// Everything that a user could be allowed to do.
public enum Permissions {
    // Events
    EVENT_VIEW("See the event catalog"),
    EVENT_CREATE("Create events"),
    EDIT_OWN_EVENTS("Can change only own events"),
    EDIT_ALL_EVENTS("Can change all events"),
    MANAGE_CATEGORIES("Create and edit event categories"),
    MANAGE_EVENT_PROVIDERS("Create and edit event providers(organizers)"),

    // Bookings
    BOOK_EVENTS("Can book an event for oneself"),
    BOOK_FOR_CUSTOMER("Can book for a customer"),
    VIEW_OWN_BOOKINGS("Can see own bookings"),
    VIEW_ALL_BOOKINGS("Can see every booking in the system"),

    // Customers
    VIEW_CUSTOMERS("Can see customer data"),
    MANAGE_CUSTOMERS("Create and edit customers"),

    // Finance
    VIEW_PAYMENTS("Can see payments"),
    MANAGE_PAYMENTS("Can process payments and refunds"),

    // Administration
    MANAGE_USERS("Can create user accounts and assign roles"),
    MANAGE_ROLES("Can create roles and decide which permissions they have");

    private final String description;

    Permissions(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }


}