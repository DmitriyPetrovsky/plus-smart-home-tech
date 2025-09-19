package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.CreateNewOrderRequest;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ProductReturnRequest;
import ru.yandex.practicum.feign.OrderOperations;
import ru.yandex.practicum.service.OrderService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/order")
public class OrderController implements OrderOperations {
    private final OrderService orderService;

    @Override
    public List<OrderDto> getUserOrders(String username) {
        log.info("Получен запрос на список заказов пользователя {}", username);
        return orderService.getUserOrders(username);
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        log.info("Получен запрос на создание нового заказа");
        return orderService.createNewOrder(request);
    }

    @Override
    public OrderDto returnProducts(ProductReturnRequest request) {
        log.info("Получен запрос на удаление товаров из заказа");
        return orderService.returnProducts(request);
    }

    @Override
    public OrderDto orderPayment(UUID orderId) {
        log.info("Заказ {} успешно оплачен", orderId);
        return orderService.orderPayment(orderId);
    }

    @Override
    public OrderDto orderPaymentFailed(UUID orderId) {
        log.info("Оплата заказа {} завершилась неудачей", orderId);
        return orderService.orderPaymentFailed(orderId);
    }

    @Override
    public OrderDto orderDelivery(UUID orderId) {
        log.info("Получен запрос на доставку заказа {}", orderId);
        return orderService.orderDelivery(orderId);
    }

    @Override
    public OrderDto orderDeliveryFailed(UUID orderId) {
        log.info("Доставка заказа {} завершилась неудачей", orderId);
        return orderService.orderDeliveryFailed(orderId);
    }

    @Override
    public OrderDto orderDeliverySuccess(UUID orderId) {
        log.info("Успешная доставка заказа {}", orderId);
        return orderService.orderDeliverySuccess(orderId);
    }

    @Override
    public OrderDto calculateTotalOrderCost(UUID orderId) {
        log.info("Получен запрос на расчет полной стоимости заказа {}", orderId);
        return orderService.calculateTotalOrderCost(orderId);
    }

    @Override
    public OrderDto calculateDeliveryCost(UUID orderId) {
        log.info("Получен запрос на расчет стоимости доставки для заказа {}", orderId);
        return orderService.calculateDeliveryCost(orderId);
    }

    @Override
    public OrderDto orderAssembly(UUID orderId) {
        log.info("Запрос на сборку заказа {}", orderId);
        return orderService.orderAssembly(orderId);
    }

    @Override
    public OrderDto orderAssemblyFailed(UUID orderId) {
        log.info("Сборка заказа {} завершилась неудачей", orderId);
        return orderService.orderAssemblyFailed(orderId);
    }

    @Override
    public OrderDto calculateProductsOrderCost(UUID orderId) {
        log.info("Получен запрос на расчет стоимости товаров заказа {}", orderId);
        return orderService.calculateProductsOrderCost(orderId);
    }

    @Override
    public OrderDto findOrderById(@RequestBody UUID orderId) {
        return orderService.findOrderById(orderId);
    }

}
