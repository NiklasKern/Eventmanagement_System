package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.application.dto.CustomerDto;
import at.fhv.eventmangement.application.form.CustomerForm;

public interface CustomerService {
    void addCustomer(CustomerForm form);
    CustomerDto getCurrentCustomer();
}
