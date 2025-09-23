package ru.yandex.practicum.feign;


import org.springframework.data.domain.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ProductCategory;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreOperations {

    @PutMapping
    ProductDto addProduct(@RequestBody ProductDto product);

    @GetMapping
    Page<ProductDto> searchProducts(@RequestParam ProductCategory category, Pageable params);

    @GetMapping("/{productId}")
    ProductDto getProductById(@PathVariable UUID productId);

    @PostMapping
    ProductDto updateProduct(@RequestBody ProductDto product);

    @PostMapping("/removeProductFromStore")
    boolean removeProduct(@RequestBody UUID productId);

    @PostMapping("/quantityState")
    boolean updateProductQuantity(@RequestBody SetProductQuantityStateRequest request);

    @PostMapping("/by-ids")
    List<ProductDto> getProductsByIds(@RequestBody List<UUID> productIds);
}
