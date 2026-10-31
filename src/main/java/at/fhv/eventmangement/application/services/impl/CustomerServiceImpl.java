package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.EventmanagementApplicationRunner;
import at.fhv.eventmangement.application.dto.CustomerDto;
import at.fhv.eventmangement.application.form.CustomerForm;
import at.fhv.eventmangement.application.mappers.CustomerMapper;
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
    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
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

    public CustomerDto getCurrentCustomer() {
        return customerMapper.toDto(customerRepository.findByContactInfoName(EventmanagementApplicationRunner.DEFAULT_CUSTOMER).orElseThrow());
    }
}
