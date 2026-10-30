package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.form.CustomerForm;
import at.fhv.eventmangement.application.services.CustomerService;
import at.fhv.eventmangement.domain.exceptions.ValidationException;
import at.fhv.eventmangement.domain.model.ContactInfo;
import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.repositories.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public void addCustomer(CustomerForm form) {

        String name = form.getName().trim();

        if(customerRepository.existsByContactInfoNameIgnoreCase(name))
        {
            throw new ValidationException(Map.of("name","customer already exists"));
        }

        customerRepository.save(Customer.builder()
                .contactInfo(ContactInfo.builder()
                        .name(name)
                        .address(form.getAddress().trim())
                        .email(form.getEmail().trim())
                        .build()
                )
                .phone(form.getPhone())
                .customerType(form.getCustomerType())
                .build()
        );
    }
}
