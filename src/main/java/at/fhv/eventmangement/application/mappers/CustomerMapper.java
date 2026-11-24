package at.fhv.eventmangement.application.mappers;

import at.fhv.eventmangement.application.dto.CustomerDto;
import at.fhv.eventmangement.domain.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",uses = {ContactInfoMapper.class})
public interface CustomerMapper {
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    Customer fromDto(CustomerDto customerDto);
    CustomerDto toDto(Customer customer);
}