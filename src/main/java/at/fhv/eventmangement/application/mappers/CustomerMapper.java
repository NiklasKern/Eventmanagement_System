package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.CustomerDto;
import at.fhv.eventmangement.domain.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",uses = {ContactInfoMapper.class})
public interface CustomerMapper {
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "created", ignore = true)
    Customer fromDto(CustomerDto customerDto);
    CustomerDto toDto(Customer customer);
}