package at.fhv.eventmangement.domain.repositories;

import at.fhv.eventmangement.domain.model.Customer;

import java.util.Optional;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findByContactInfoName(String name);
    boolean existsByContactInfoNameIgnoreCase(String name);
}
