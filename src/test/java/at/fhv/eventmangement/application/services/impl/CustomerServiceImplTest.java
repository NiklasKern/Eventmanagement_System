package at.fhv.eventmangement.application.services.impl;

import at.fhv.eventmangement.application.dto.CustomerDto;
import at.fhv.eventmangement.application.mappers.CustomerMapper;
import at.fhv.eventmangement.domain.exceptions.CustomerNotFoundException;
import at.fhv.eventmangement.domain.model.ContactInfo;
import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.model.UserAccount;
import at.fhv.eventmangement.domain.repositories.CustomerRepository;
import at.fhv.eventmangement.domain.repositories.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private final Customer anna = Customer.builder()
            .id(UUID.randomUUID())
            .contactInfo(new ContactInfo("Anna", "Street 1", "anna@test.com"))
            .build();

    private void loggedIn(Customer linkedCustomer) {
        when(userAccountRepository.findByUsername("anna")).thenReturn(Optional.of(UserAccount.builder()
                .username("anna")
                .enabled(true)
                .customer(linkedCustomer)
                .build()));
    }

    @Test
    void getCurrentCustomer_returnsCustomerLinkedToAccount() throws Exception {
        CustomerDto dto = mock(CustomerDto.class);
        loggedIn(anna);
        when(customerMapper.toDto(anna)).thenReturn(dto);

        assertThat(customerService.getCurrentCustomer("anna")).isSameAs(dto);
    }

    @Test
    void getCurrentCustomer_accountWithoutCustomer_isRejected() {
        loggedIn(null);

        assertThatThrownBy(() -> customerService.getCurrentCustomer("anna"))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Your account is not linked to a customer");
    }

    @Test
    void getCurrentCustomer_unknownUser_isAnError() {
        when(userAccountRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCurrentCustomer("ghost"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getAllCustomers_mapsEveryCustomer() {
        CustomerDto dto = mock(CustomerDto.class);
        when(customerRepository.findAll()).thenReturn(List.of(anna));
        when(customerMapper.toDto(anna)).thenReturn(dto);

        assertThat(customerService.getAllCustomers()).containsExactly(dto);
    }
}