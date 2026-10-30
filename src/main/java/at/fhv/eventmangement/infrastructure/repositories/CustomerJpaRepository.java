package at.fhv.eventmangement.infrastructure.repositories;

import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.domain.repositories.CustomerRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomerJpaRepository extends JpaRepository<Customer, UUID>, CustomerRepository {
}
