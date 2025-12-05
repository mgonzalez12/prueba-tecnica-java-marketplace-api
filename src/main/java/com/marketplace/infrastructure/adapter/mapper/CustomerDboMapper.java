package com.marketplace.infrastructure.adapter.mapper;

import com.marketplace.domain.model.Customer;
import com.marketplace.infrastructure.adapter.entity.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerDboMapper {

    CustomerEntity toEntity(Customer domain);

    Customer toDomain(CustomerEntity entity);
}
