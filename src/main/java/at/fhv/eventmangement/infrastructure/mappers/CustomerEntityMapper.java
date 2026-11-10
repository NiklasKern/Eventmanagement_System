package at.fhv.eventmangement.infrastructure.mappers;

import at.fhv.eventmangement.domain.model.Customer;
import at.fhv.eventmangement.infrastructure.entities.CustomerEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerEntityMapper {
    Customer toDomain(CustomerEntity entity);
    CustomerEntity toEntity(Customer domain);
}