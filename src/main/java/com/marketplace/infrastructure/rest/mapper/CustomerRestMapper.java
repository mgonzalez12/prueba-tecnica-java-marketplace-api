package com.marketplace.infrastructure.rest.mapper;

import com.marketplace.domain.model.Customer;
import com.marketplace.infrastructure.rest.dto.CustomerRequestDto;
import com.marketplace.infrastructure.rest.dto.CustomerResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "lastActivityDate", ignore = true)
    @Mapping(target = "totalOrders", ignore = true)
    @Mapping(target = "totalSpent", ignore = true)
    Customer toDomain(CustomerRequestDto request);

    default CustomerResponseDto toResponse(Customer domain) {
        if (domain == null) {
            return null;
        }
        
        String registrationDateStr = domain.getRegistrationDate() != null 
            ? domain.getRegistrationDate().toString() 
            : null;
        
        return new CustomerResponseDto(
            domain.getId(),
            domain.getFirstName(),
            domain.getLastName(),
            domain.getEmail(),
            domain.getStatus(),
            domain.getAge(),
            domain.getBirthDate(),
            registrationDateStr,
            domain.getLastActivityDate(),
            domain.getRenewalDate(),
            domain.getPhone(),
            domain.getAddress(),
            domain.getTotalOrders(),
            domain.getTotalSpent(),
            domain.getSeniorityInDaysLegacy(),
            domain.getSeniorityInYearsModern()
        );
    }
}
