package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.feign.DeliveryOperations;
import ru.yandex.practicum.feign.ShoppingStoreOperations;
import ru.yandex.practicum.feign.WarehouseOperations;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final WarehouseOperations warehouseOperations;
    private final ShoppingStoreOperations shoppingStoreOperations;
    private final DeliveryOperations deliveryOperations;

    public List<OrderDto> getUserOrders(String username) {
        return orderRepository.findByUsername(username).stream()
                .map(OrderMapper::mapToOrderDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto orderPayment(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Заказ с ID " + orderId + " не найден!"));
        order.setOrderState(OrderState.PAID);
        return OrderMapper.mapToOrderDto(orderRepository.save(order));
    }

    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        Order order = new Order();
        order.setCartId(request.getShoppingCart().getShoppingCartId());
        order.setProducts(request.getShoppingCart().getProducts());
        BookedProductsDto bookedProductsDto = warehouseOperations.checkShoppingCart(request.getShoppingCart());
        order.setVolume(bookedProductsDto.getDeliveryVolume());
        order.setWeight(bookedProductsDto.getDeliveryWeight());
        order.setFragile(bookedProductsDto.getFragile());
        order.setOrderState(OrderState.NEW);
        order.setUsername(request.getShoppingCart().getUsername());
        order.setProductPrice(calculateProductsCost(request.getShoppingCart().getProducts()));
        order.setDeliveryPrice(deliveryOperations.deliveryCost(OrderMapper.mapToOrderDto(order)));
        order.setTotalPrice(order.getProductPrice() + order.getDeliveryPrice());
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto returnProducts(ProductReturnRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException("Заказ с ID " + request.getOrderId() + " не найден!"));
        Map<UUID, Integer> products = order.getProducts();
        warehouseOperations.returnProducts(products);
        order.setOrderState(OrderState.PRODUCT_RETURNED);
        orderRepository.save(order);
        return OrderMapper.mapToOrderDto(order);
    }

    public OrderDto calculateProductsOrderCost(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID " + orderId + " не найден!"));
        order.setProductPrice(calculateProductsCost(order.getProducts()));
        return OrderMapper.mapToOrderDto(order);
    }

    public Double calculateProductsCost(Map<UUID, Integer> products) {
        return products.entrySet().stream()
                .map(entry -> {
                    UUID productId = entry.getKey();
                    Integer quantity = entry.getValue();
                    Double productPrice = shoppingStoreOperations.getProductById(productId).getPrice();
                    return quantity * productPrice;
                })
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public OrderDto orderDeliverySuccess(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID " + orderId + " не найден!"));
        order.setOrderState(OrderState.DELIVERED);
        return OrderMapper.mapToOrderDto(orderRepository.save(order));
    }

    public OrderDto orderPaymentFailed(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID " + orderId + " не найден!"));
        order.setOrderState(OrderState.PAYMENT_FAILED);
        return OrderMapper.mapToOrderDto(orderRepository.save(order));
    }

    public OrderDto orderDelivery(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID " + orderId + " не найден!"));
        order.setOrderState(OrderState.ON_DELIVERY);
        return OrderMapper.mapToOrderDto(orderRepository.save(order));
    }

    public OrderDto orderDeliveryFailed(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID " + orderId + " не найден!"));
        order.setOrderState(OrderState.DELIVERY_FAILED);
        return OrderMapper.mapToOrderDto(orderRepository.save(order));
    }

    public OrderDto calculateTotalOrderCost(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID " + orderId + " не найден!"));
        double productsCost = calculateProductsCost(order.getProducts());
        order.setProductPrice(productsCost);
        double deliveryCost = calculateDeliveryCost(orderId).getDeliveryPrice();
        order.setDeliveryPrice(deliveryCost);
        order.setTotalPrice(productsCost + deliveryCost);
        return OrderMapper.mapToOrderDto(orderRepository.save(order));
    }

    public OrderDto calculateDeliveryCost(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID " + orderId + " не найден!"));
        order.setDeliveryPrice(deliveryOperations.deliveryCost(OrderMapper.mapToOrderDto(order)));
        return OrderMapper.mapToOrderDto(orderRepository.save(order));
    }

    public OrderDto orderAssembly(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID " + orderId + " не найден!"));
        order.setOrderState(OrderState.ASSEMBLED);
        return OrderMapper.mapToOrderDto(orderRepository.save(order));
    }

    public OrderDto orderAssemblyFailed(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID " + orderId + " не найден!"));
        order.setOrderState(OrderState.ASSEMBLY_FAILED);
        return OrderMapper.mapToOrderDto(orderRepository.save(order));    }
}
