package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.model.ShoppingCart;

import java.util.HashMap;

public class ShoppingCartMapper {
    public static ShoppingCartDto mapToShoppingCartDto(ShoppingCart cart) {
        if (cart == null) {
            return null;
        }
        return new ShoppingCartDto(
                cart.getShoppingCartId(),
                cart.getProducts() != null ? new HashMap<>(cart.getProducts()) : new HashMap<>()
        );
    }
}
