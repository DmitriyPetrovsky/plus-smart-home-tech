package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.dto.OrderState;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "order_id")
    @EqualsAndHashCode.Include
    private UUID orderId;
    @Column(name = "username", nullable = false, length = 255)
    private String username;
    @Column(name = "cart_id", nullable = false)
    private UUID cartId;
    @Column(name = "payment_id")
    private UUID paymentId;
    @Column(name = "delivery_id")
    private UUID deliveryId;
    @Enumerated(EnumType.STRING)
    @Column(name = "order_state", nullable = false, length = 50)
    private OrderState orderState;
    @Column(name = "weight")
    private Double weight;
    @Column(name = "volume")
    private Double volume;
    @Column(name = "fragile")
    private Boolean fragile;
    @Column(name = "total_price")
    private Double totalPrice;
    @Column(name = "delivery_price")
    private Double deliveryPrice;
    @Column(name = "product_price")
    private Double productPrice;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "order_products",
            joinColumns = @JoinColumn(name = "order_id")
    )
    @MapKeyColumn(name = "product_id", columnDefinition = "UUID")
    @Column(name = "quantity")
    private Map<UUID, Integer> products;
}
