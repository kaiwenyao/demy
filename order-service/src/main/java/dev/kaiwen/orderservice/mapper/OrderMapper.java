package dev.kaiwen.orderservice.mapper;

import dev.kaiwen.orderservice.dto.OrderResponse;
import dev.kaiwen.orderservice.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponse orderToResponse(Order order);
}

