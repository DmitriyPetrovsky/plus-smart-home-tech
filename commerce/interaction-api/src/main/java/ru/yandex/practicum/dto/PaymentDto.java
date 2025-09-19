package ru.yandex.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    @NotNull
    private UUID paymentId;
    @NotNull
    private UUID orderId;
    @NotNull
    private Double totalPayment;
    @NotNull
    private Double deliveryTotal;
    @NotNull
    private Double feeTotal;
    private PaymentState paymentState;
}
