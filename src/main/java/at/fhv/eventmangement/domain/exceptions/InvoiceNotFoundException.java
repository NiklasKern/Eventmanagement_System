package at.fhv.eventmangement.domain.exceptions;

public class InvoiceNotFoundException extends EventManagementException {
    public InvoiceNotFoundException() {
        super("invoice does not exist");
    }
}
