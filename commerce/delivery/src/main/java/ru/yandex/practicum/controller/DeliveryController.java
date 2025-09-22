package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.feign.DeliveryOperations;
import ru.yandex.practicum.service.DeliveryService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController implements DeliveryOperations {
    private final DeliveryService deliveryService;

    @Override
    public DeliveryDto createDelivery(@Valid @RequestBody DeliveryDto deliveryDto) {
        log.info("Получен запрос на создание новой доставки к заказу с ID: {}", deliveryDto.getOrderId());
        return deliveryService.createDelivery(deliveryDto);
    }

    @Override
    public void successful(@RequestBody UUID deliveryId) {
        log.info("Эмуляция успешной доставки ID: {}", deliveryId);
        deliveryService.successful(deliveryId);
    }

    @Override
    public void picked(@RequestBody UUID deliveryId) {
        log.info("Эмуляция получения товара в доставку c ID: {}", deliveryId);
        deliveryService.picked(deliveryId);
    }

    @Override
    public void failed(@RequestBody UUID deliveryId) {
        log.info("Эмуляция неудачного вручения товара доставки с ID: {}", deliveryId);
        deliveryService.failed(deliveryId);
    }

    @Override
    public double deliveryCost(@Valid @RequestBody OrderDto orderDto) {
        log.info("Получен запрос на расчёт полной стоимости доставки заказа c ID: {}", orderDto.getOrderId());
        return deliveryService.deliveryCostCalculation(orderDto);
    }
}