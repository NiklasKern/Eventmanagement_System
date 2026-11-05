package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.application.dto.CustomerDto;
import at.fhv.eventmangement.domain.exceptions.CustomerAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.application.form.CustomerForm;

public interface CustomerService {
    void addCustomer(CustomerForm form) throws CustomerAlreadyExistsException;
    CustomerDto getCurrentCustomer() throws CustomerNotFoundException;
}
