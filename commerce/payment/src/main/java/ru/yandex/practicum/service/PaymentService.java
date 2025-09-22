package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.PaymentState;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.feign.OrderOperations;
import ru.yandex.practicum.feign.ShoppingStoreOperations;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.repository.PaymentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderOperations orderOperations;
    private final ShoppingStoreOperations shoppingStoreOperations;

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
        if (orderDto.getProducts() == null || orderDto.getProducts().isEmpty()) {
            return 0.0;
        }
        List<UUID> productIds = new ArrayList<>(orderDto.getProducts().keySet());
        List<ProductDto> productDtos = shoppingStoreOperations.getProductsByIds(productIds);
        Map<UUID, Double> productPrices = productDtos.stream()
                .collect(Collectors.toMap(ProductDto::getProductId, ProductDto::getPrice));
        return orderDto.getProducts().entrySet().stream()
                .mapToDouble(entry -> {
                    UUID productId = entry.getKey();
                    Integer quantity = entry.getValue();
                    Double price = productPrices.getOrDefault(productId, 0.0);
                    return quantity * price;
                })
                .sum();
    }

    private void checkPrices(OrderDto orderDto) {
        if (orderDto.getDeliveryPrice() == null  || orderDto.getDeliveryPrice() <= 0
        || orderDto.getTotalPrice() == null || orderDto.getTotalPrice() <= 0
        || orderDto.getProductPrice() == null || orderDto.getProductPrice() <= 0) {
            throw new NotEnoughInfoInOrderToCalculateException("Недостаточно информации для расчета");
        }
    }
}
