package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import ru.yandex.practicum.dto.PaymentState;

import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment")
public class Payment {

    @Id
    @UuidGenerator
    private UUID paymentId;

    private UUID orderId;

    private double totalPayment;

    private double deliveryTotal;

    private double feeTotal;

    @Enumerated(EnumType.STRING)
    private PaymentState paymentState;

}
