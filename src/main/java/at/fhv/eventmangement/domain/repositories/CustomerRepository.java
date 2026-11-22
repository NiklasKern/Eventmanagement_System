package at.fhv.eventmangement.domain.repositories;

import at.fhv.eventmangement.domain.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Customer save(Customer customer);

    Optional<Customer> findByContactInfoName(String name);
    Optional<Customer> findById(UUID id);
    boolean existsByContactInfoNameIgnoreCase(String name);
    List<Customer> findAll();
}
