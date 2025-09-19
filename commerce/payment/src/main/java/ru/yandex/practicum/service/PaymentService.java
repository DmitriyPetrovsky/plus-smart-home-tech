package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.PaymentState;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.feign.OrderOperations;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.repository.PaymentRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderOperations orderOperations;

    @Transactional
    public PaymentDto createPayment(OrderDto orderDto) {
        checkPrices(orderDto);
        Payment payment = PaymentMapper.mapToPayment(orderDto);
        return PaymentMapper.mapToPaymentDto(paymentRepository.save(payment));
    }

    public double getTotalCost(OrderDto orderDto) {
        checkPrices(orderDto);
        Payment payment = PaymentMapper.mapToPayment(orderDto);
        return payment.getTotalPayment();
    }

    public void refund(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Платеж не найден"));
        payment.setPaymentState(PaymentState.SUCCESS);
        paymentRepository.save(payment);
        orderOperations.orderPayment(payment.getOrderId());
    }

    public void failed(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Платеж не найден"));
        payment.setPaymentState(PaymentState.FAILED);
        paymentRepository.save(payment);
        orderOperations.orderPaymentFailed(payment.getOrderId());
    }

    public double productCost(OrderDto orderDto) {
        return orderOperations.calculateProductsOrderCost(orderDto.getOrderId()).getProductPrice();
    }

    private void checkPrices(OrderDto orderDto) {
        if (orderDto.getDeliveryPrice() == null  || orderDto.getDeliveryPrice() <= 0
        || orderDto.getTotalPrice() == null || orderDto.getTotalPrice() <= 0
        || orderDto.getProductPrice() == null || orderDto.getProductPrice() <= 0) {
            throw new NotEnoughInfoInOrderToCalculateException("Недостаточно информации для расчета");
        }
    }
}
