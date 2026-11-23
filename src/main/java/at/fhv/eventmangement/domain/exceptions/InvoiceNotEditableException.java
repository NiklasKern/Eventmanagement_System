package at.fhv.eventmangement.domain.exceptions;

public class InvoiceNotEditableException extends EventManagementException {
    public InvoiceNotEditableException() {
        super("invoice is already final and cannot be changed");
    }
}
