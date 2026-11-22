package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.repositories.CustomerRepository;
import at.fhv.eventmangement.infrastructure.mappers.CustomerEntityMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository jpa;
    private final CustomerEntityMapper mapper;

    public CustomerRepositoryAdapter(CustomerJpaRepository jpa, CustomerEntityMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Customer save(Customer customer) {
        return mapper.toDomain(jpa.save(mapper.toEntity(customer)));
    }

    @Override
    public Optional<Customer> findByContactInfoName(String name) {
        return jpa.findByContactInfoName(name).map(mapper::toDomain);
    }

    @Override
    public boolean existsByContactInfoNameIgnoreCase(String name) {
        return jpa.existsByContactInfoNameIgnoreCase(name);
    }

    @Override
    public List<Customer> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }
}
