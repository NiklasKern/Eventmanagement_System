package at.fhv.eventmangement.application.services;

import at.fhv.eventmangement.application.dto.CustomerDto;
import at.fhv.eventmangement.application.form.CustomerForm;
import at.fhv.eventmangement.domain.exceptions.CustomerAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;

import java.util.List;

public interface CustomerService {
    void addCustomer(CustomerForm form) throws CustomerAlreadyExistsException;

    // Customer linked to the account of the logged-in user
    CustomerDto getCurrentCustomer(String username) throws CustomerNotFoundException;

    // For the "linked customer" dropdown in the user manager
    List<CustomerDto> getAllCustomers();
}