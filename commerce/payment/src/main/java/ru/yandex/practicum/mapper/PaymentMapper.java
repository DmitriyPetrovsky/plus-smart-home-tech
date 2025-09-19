package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.PaymentState;
import ru.yandex.practicum.model.Payment;

public class PaymentMapper {

    public static PaymentDto mapToPaymentDto(Payment payment) {
        if (payment == null) {
            return null;
        }
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setPaymentId(payment.getPaymentId());
        paymentDto.setOrderId(payment.getOrderId());
        paymentDto.setTotalPayment(payment.getTotalPayment());
        paymentDto.setDeliveryTotal(payment.getDeliveryTotal());
        paymentDto.setFeeTotal(payment.getFeeTotal());
        paymentDto.setPaymentState(payment.getPaymentState());
        return paymentDto;
    }

    public static Payment mapToPayment(OrderDto orderDto) {
        if (orderDto == null) {
            return null;
        }
        double productPrice = orderDto.getProductPrice() != null ? orderDto.getProductPrice() : 0.0;
        double feeTotal = orderDto.getProductPrice() != null ? orderDto.getProductPrice() * 0.1 : 0.0;
        double deliveryTotal = orderDto.getDeliveryPrice() != null ? orderDto.getDeliveryPrice() : 0.0;
        double totalPayment = orderDto.getTotalPrice() != null ? productPrice + deliveryTotal + feeTotal : 0.0;
        Payment payment = new Payment();
        payment.setOrderId(orderDto.getOrderId());
        payment.setDeliveryTotal(deliveryTotal);
        payment.setFeeTotal(feeTotal);
        payment.setTotalPayment(totalPayment);
        payment.setPaymentState(PaymentState.PENDING);
        return payment;
    }
}
