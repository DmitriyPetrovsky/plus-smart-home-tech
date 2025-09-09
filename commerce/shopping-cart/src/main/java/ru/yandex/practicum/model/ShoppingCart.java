package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@Table(name = "cart")
public class ShoppingCart {

    @Id
    @UuidGenerator
    private UUID shoppingCartId;

    @ElementCollection
    @CollectionTable(name = "cart_products", joinColumns = @JoinColumn(name = "cart_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Integer> products;

    private String username;

    @Enumerated(EnumType.STRING)
    private ShoppingCartState state;
}
