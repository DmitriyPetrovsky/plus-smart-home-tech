package ru.yandex.practicum.feign;


import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ProductCategory;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;

import java.util.UUID;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreOperations {

    @PutMapping
    ProductDto addProduct(@Valid @RequestBody ProductDto product);

    @GetMapping
    Page<ProductDto> searchProducts(ProductCategory category, Pageable params);

    @GetMapping("/{productId}")
    ProductDto getProductById(@PathVariable UUID productId);

    @PostMapping
    ProductDto updateProduct(@Valid @RequestBody ProductDto product);

    @PostMapping("/removeProductFromStore")
    boolean removeProduct(@RequestBody UUID productId);


    @PostMapping("/quantityState")
    boolean updateProductQuantity(@Valid SetProductQuantityStateRequest request);
}
