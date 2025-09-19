package ru.yandex.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewOrderRequest {
    @NotNull
    ShoppingCartDto shoppingCart;
    @NotNull
    AddressDto deliveryAddress;
}
