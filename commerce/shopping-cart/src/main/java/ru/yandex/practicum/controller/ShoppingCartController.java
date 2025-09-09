package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.feign.ShoppingCartOperations;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/shopping-cart")
public class ShoppingCartController implements ShoppingCartOperations {
    private final ShoppingCartService shoppingCartService;

    @Override
    public ShoppingCartDto getUsersShoppingCart(String username) {
        log.info("Получен запрос на выдачу корзины пользователя: {}", username);
        return shoppingCartService.getUsersShoppingCart(username);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Integer> products) {
        log.info("Получен запрос на добавление товара в корзину");
        return shoppingCartService.addProductToShoppingCart(username, products);
    }

    @Override
    public void deactivateShoppingCart(String username) {
        log.info("Получен запрос на деактивацию корзины пользователя: {}", username);
        shoppingCartService.deactivateShoppingCart(username);
    }

    @Override
    public ShoppingCartDto removeProductFromShoppingCart(String username, List<UUID> products) {
        log.info("Получен запрос на удаление товаров из корзины пользователя: {}", username);
        return shoppingCartService.removeProductsFromShoppingCart(username, products);
    }

    @Override
    public ShoppingCartDto changeProductQuantityInCart(String username, ChangeProductQuantityRequest request) {
        log.info("Получен запрос на изменение количества товара в корзине пользователя: {}", username);
        return shoppingCartService.changeProductQuantityInCart(username, request);
    }
}
