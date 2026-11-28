package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.dto.CustomerDto;
import at.fhv.eventmangement.application.form.CustomerForm;
import at.fhv.eventmangement.application.mappers.CustomerMapper;
import at.fhv.eventmangement.application.services.CustomerService;
import at.fhv.eventmangement.domain.exceptions.CustomerAlreadyExistsException;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.model.ContactInfo;
import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.model.UserAccount;
import at.fhv.eventmangement.domain.repositories.CustomerRepository;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserAccountRepository userAccountRepository;
    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               UserAccountRepository userAccountRepository,
                               CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.userAccountRepository = userAccountRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCustomer(CustomerForm form) throws CustomerAlreadyExistsException {

        String name = form.getName().trim();

        if (customerRepository.existsByContactInfoNameIgnoreCase(name)) {
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

    // Customer linked to the account of the logged-in user
    @Override
    public CustomerDto getCurrentCustomer(String username) throws CustomerNotFoundException {
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("No account for logged-in user " + username));
        if (account.getCustomer() == null) {
            throw new CustomerNotFoundException("Your account is not linked to a customer");
        }
        return customerMapper.toDto(account.getCustomer());
    }

    @Override
    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findAll().stream().map(customerMapper::toDto).toList();
    }
}