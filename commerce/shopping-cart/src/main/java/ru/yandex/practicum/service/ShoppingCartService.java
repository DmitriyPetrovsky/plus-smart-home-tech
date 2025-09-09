package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.feign.WarehouseOperations;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.model.ShoppingCartState;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final WarehouseOperations warehouseClient;

    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Integer> products) {
        validateUsername(username);
        ShoppingCart shoppingCart = getShoppingCart(username);
        Map<UUID, Integer> oldProducts = shoppingCart.getProducts();
        oldProducts.putAll(products);
        shoppingCart.setProducts(oldProducts);
        ShoppingCartDto cartDto = ShoppingCartMapper.mapToShoppingCartDto(shoppingCart);
        try {
            warehouseClient.checkShoppingCart(cartDto);
        } catch (Exception e) {

        }
        shoppingCartRepository.save(shoppingCart);
        return cartDto;
    }

    @Transactional(readOnly = true)
    public ShoppingCartDto getUsersShoppingCart(String username) {
        validateUsername(username);
        ShoppingCart shoppingCart = getShoppingCart(username);
        return ShoppingCartMapper.mapToShoppingCartDto(shoppingCart);
    }

    @Transactional
    public void deactivateShoppingCart(String username) {
        validateUsername(username);
        ShoppingCart shoppingCart = getShoppingCart(username);
        shoppingCart.setState(ShoppingCartState.DEACTIVATE);
        shoppingCartRepository.save(shoppingCart);
    }

    @Transactional
    public ShoppingCartDto removeProductsFromShoppingCart(String username, List<UUID> products) {
        validateUsername(username);
        ShoppingCart shoppingCart = getShoppingCart(username);
        Map<UUID, Integer> productMap = shoppingCart.getProducts();
        products.forEach(productMap::remove);
        shoppingCart.setProducts(productMap);
        shoppingCartRepository.save(shoppingCart);
        return ShoppingCartMapper.mapToShoppingCartDto(shoppingCart);
    }

    @Transactional
    public ShoppingCartDto changeProductQuantityInCart(String username, ChangeProductQuantityRequest request) {
        validateUsername(username);
        ShoppingCart shoppingCart = getShoppingCart(username);
        Map<UUID, Integer> cartProducts = shoppingCart.getProducts();
        cartProducts.put(request.getProductId(), request.getNewQuantity());
        shoppingCart.setProducts(cartProducts);
        ShoppingCartDto cartDto = ShoppingCartMapper.mapToShoppingCartDto(shoppingCart);
        try {
            warehouseClient.checkShoppingCart(cartDto);
        } catch (Exception e) {

        }
        shoppingCartRepository.save(shoppingCart);
        return cartDto;
    }

    private void validateUsername(String username) {
        if (username.isBlank()) {
            throw new NotAuthorizedUserException("Отсутствует информация о пользователе в запросе");
        }
    }

    private ShoppingCart getShoppingCart(String username) {
        return shoppingCartRepository.findByUsernameAndState(username, ShoppingCartState.ACTIVE)
                .orElseGet(() -> {
                    ShoppingCart newShoppingCart = new ShoppingCart();
                    newShoppingCart.setUsername(username);
                    newShoppingCart.setProducts(new HashMap<>());
                    newShoppingCart.setState(ShoppingCartState.ACTIVE);
                    return shoppingCartRepository.save(newShoppingCart);
                });
    }
}
