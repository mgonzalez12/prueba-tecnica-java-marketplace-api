package com.marketplace.infrastructure.rest.mapper;

import com.marketplace.domain.model.Customer;
import com.marketplace.infrastructure.rest.dto.generated.CustomerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerApiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE") // Default status
    @Mapping(target = "registrationDate", expression = "java(new java.util.Date())") // Map createdAt to registrationDate
    @Mapping(target = "lastActivityDate", ignore = true)
    @Mapping(target = "renewalDate", ignore = true)
    @Mapping(target = "totalOrders", ignore = true)
    @Mapping(target = "totalSpent", ignore = true)
    @Mapping(target = "address", ignore = true)
    Customer toDomain(CustomerRequest request);

    com.marketplace.infrastructure.rest.dto.generated.Customer toApiDto(Customer domain);
}

