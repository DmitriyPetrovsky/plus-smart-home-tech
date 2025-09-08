package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.ProductCategory;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.feign.ShoppingStoreOperations;
import ru.yandex.practicum.service.ShoppingStoreService;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/shopping-store")
public class ShoppingStoreController implements ShoppingStoreOperations {
    private final ShoppingStoreService shoppingService;

    @Override
    public Page<ProductDto> searchProducts(ProductCategory category, Pageable params) {
        log.info("Получен запрос на поиск товара, параметры:");
        log.info("категория товара: {}", category);
        log.info("параметры вывода: {}", params);
        return shoppingService.getProductsByCategory(category, params);
    }

    @Override
    public ProductDto addProduct(ProductDto product) {
        return shoppingService.addProduct(product);
    }

    @Override
    public ProductDto getProductById(UUID productId) {
        return shoppingService.findProductById(productId);
    }

    @Override
    public ProductDto updateProduct(ProductDto product) {
        return shoppingService.updateProduct(product);
    }

    @Override
    public boolean removeProduct(UUID productId) {
        shoppingService.removeProductFromStore(productId);
        return true;
    }


    @Override
    public boolean updateProductQuantity(SetProductQuantityStateRequest request) {
        shoppingService.setProductQuantityState(request);
        return true;
    }
}
