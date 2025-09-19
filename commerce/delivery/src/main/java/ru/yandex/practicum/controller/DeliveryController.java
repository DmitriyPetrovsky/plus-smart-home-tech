package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        log.info("Получен запрос на создание новой доставки");
        return deliveryService.createDelivery(deliveryDto);
    }

    @Override
    public void successful(UUID deliveryId) {
        log.info("Эмуляция успешной доставки");
        deliveryService.successful(deliveryId);
    }

    @Override
    public void picked(UUID deliveryId) {
        log.info("Эмуляция получения товара в доставку");
        deliveryService.picked(deliveryId);
    }

    @Override
    public void failed(UUID deliveryId) {
        log.info("Эмуляция неудачного вручения товара");
        deliveryService.failed(deliveryId);
    }

    @Override
    public double deliveryCost(OrderDto orderDto) {
        log.info("Получен запрос на расчёт полной стоимости доставки заказа");
        return deliveryService.deliveryCostCalculation(orderDto);
    }
}
