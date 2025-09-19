package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.model.Order;


public class OrderMapper {
    public static OrderDto mapToOrderDto(Order order) {
        if (order == null) {
            return null;
        }

        return OrderDto.builder()
                .orderId(order.getOrderId())
                .username(order.getUsername())
                .shoppingCartId(order.getCartId())
                .paymentId(order.getPaymentId())
                .deliveryId(order.getDeliveryId())
                .state(order.getOrderState())
                .weight(order.getWeight())
                .volume(order.getVolume())
                .fragile(order.getFragile())
                .totalPrice(order.getTotalPrice())
                .deliveryPrice(order.getDeliveryPrice())
                .productPrice(order.getProductPrice())
                .products(order.getProducts())
                .build();
    }

}
