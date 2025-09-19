package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.feign.PaymentOperations;
import ru.yandex.practicum.service.PaymentService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController implements PaymentOperations {
    private final PaymentService paymentService;

    @Override
    public PaymentDto createPayment(OrderDto orderDto) {
        log.info("Получен запрос на формирование оплаты для заказа с ID: {}", orderDto.getOrderId());
        return paymentService.createPayment(orderDto);
    }

    @Override
    public double getTotalCost(OrderDto orderDto) {
        log.info("Получен запрос на расчет полной стоимости заказа с ID: {}", orderDto.getOrderId());
        return paymentService.getTotalCost(orderDto);
    }

    @Override
    public void refund(UUID paymentId) {
        log.info("Эмуляция успешной оплаты");
        paymentService.refund(paymentId);
    }

    @Override
    public double productCost(OrderDto orderDto) {
        log.info("Получен запрос на расчет стоимости товаров в заказе с ID: {}", orderDto.getOrderId());
        return paymentService.productCost(orderDto);
    }

    @Override
    public void failed(UUID paymentId) {
        log.info("Эмуляция отказа в оплате");
        paymentService.failed(paymentId);
    }

}
