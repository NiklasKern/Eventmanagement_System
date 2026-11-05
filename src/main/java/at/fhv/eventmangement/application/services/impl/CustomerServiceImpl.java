package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.EventmanagementApplicationRunner;
import at.fhv.eventmangement.application.dto.CustomerDto;
import at.fhv.eventmangement.domain.exceptions.CustomerAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.application.form.CustomerForm;
import at.fhv.eventmangement.application.mappers.CustomerMapper;
import at.fhv.eventmangement.application.services.CustomerService;
import at.fhv.eventmangement.domain.model.ContactInfo;
import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.repositories.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCustomer(CustomerForm form) throws CustomerAlreadyExistsException {

        String name = form.getName().trim();

        if(customerRepository.existsByContactInfoNameIgnoreCase(name))
        {
            throw new CustomerAlreadyExistsException();
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

    @Override
    public CustomerDto getCurrentCustomer() throws CustomerNotFoundException {
        return customerMapper.toDto(customerRepository.findByContactInfoName(EventmanagementApplicationRunner.DEFAULT_CUSTOMER)
                .orElseThrow(CustomerNotFoundException::new));
    }
}
