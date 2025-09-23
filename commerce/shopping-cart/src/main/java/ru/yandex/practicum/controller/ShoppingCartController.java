package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ShoppingCartDto getShoppingCartById(@RequestParam String username) {
        log.info("Получен запрос на выдачу корзины пользователя: {}", username);
        return shoppingCartService.getUsersShoppingCart(username);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(@RequestParam String username,
                                                    @RequestBody Map<UUID, Integer> products) {
        log.info("Получен запрос на добавление товара в корзину");
        return shoppingCartService.addProductToShoppingCart(username, products);
    }

    @Override
    public void deactivateShoppingCart(@RequestParam String username) {
        log.info("Получен запрос на деактивацию корзины пользователя: {}", username);
        shoppingCartService.deactivateShoppingCart(username);
    }

    @Override
    public ShoppingCartDto removeProductFromShoppingCart(@RequestParam String username,
                                                         @RequestBody List<UUID> products) {
        log.info("Получен запрос на удаление товаров из корзины пользователя: {}", username);
        return shoppingCartService.removeProductsFromShoppingCart(username, products);
    }

    @Override
    public ShoppingCartDto changeProductQuantityInCart(@RequestParam String username,
                                                       @Valid @RequestBody ChangeProductQuantityRequest request) {
        log.info("Получен запрос на изменение количества товара в корзине пользователя: {}", username);
        return shoppingCartService.changeProductQuantityInCart(username, request);
    }

    @Override
    public ShoppingCartDto getShoppingCartById(@RequestParam UUID shoppingCartId) {
        log.info("Получен запрос на выдачу корзины по ID: {}", shoppingCartId);
        return shoppingCartService.getShoppingCartById(shoppingCartId);
    }
}
