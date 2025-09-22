package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ProductCategory;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.feign.ShoppingStoreOperations;
import ru.yandex.practicum.service.ShoppingStoreService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/shopping-store")
public class ShoppingStoreController implements ShoppingStoreOperations {
    private final ShoppingStoreService shoppingService;

    @Override
    public Page<ProductDto> searchProducts(@RequestParam ProductCategory category, Pageable params) {
        log.info("Получен запрос на поиск товаров по категории: {}", category);
        return shoppingService.getProductsByCategory(category, params);
    }

    @Override
    public ProductDto addProduct(@Valid @RequestBody ProductDto product) {
        log.info("Получен запрос на добавление товара");
        return shoppingService.addProduct(product);
    }

    @Override
    public ProductDto getProductById(@PathVariable UUID productId) {
        log.info("Получен запрос на получение товара по ID: {}", productId);
        return shoppingService.findProductById(productId);
    }

    @Override
    public ProductDto updateProduct(@Valid @RequestBody ProductDto product) {
        log.info("Получен запрос на обновление информации о товаре с ID: {}", product.getProductId());
        return shoppingService.updateProduct(product);
    }

    @Override
    public boolean removeProduct(@RequestBody UUID productId) {
        log.info("Получен запрос на удаление информации о товаре с ID: {}", productId);
        shoppingService.removeProductFromStore(productId);
        return true;
    }

    @Override
    public boolean updateProductQuantity(@Valid @RequestBody SetProductQuantityStateRequest request) {
        log.info("Получен запрос на измененине информации о количестве товара с ID: {}", request.getProductId());
        shoppingService.setProductQuantityState(request);
        return true;
    }

    @PostMapping("/by-ids")
    public List<ProductDto> getProductsByIds(@RequestBody List<UUID> productIds) {
        log.info("Получен запрос на получение товаров по IDs: {}", productIds);
        return shoppingService.getProductsByIds(productIds);
    }
}
