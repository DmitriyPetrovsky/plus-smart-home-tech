package ru.yandex.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "warehouse")
public interface WarehouseOperations {

    @PutMapping
    void addProductToWarehouse(@Valid @RequestBody NewProductInWarehouseRequest request);

    @PostMapping("/check")
    BookedProductsDto checkShoppingCart(@Valid @RequestBody ShoppingCartDto shoppingCartDto);

    @PostMapping("/add")
    void increaseProductQuantity(@Valid @RequestBody AddProductToWarehouseRequest request);

    @GetMapping("/address")
    AddressDto getWarehouseAddress();

    @PostMapping("/return")
    void returnProducts(@RequestBody Map<UUID, Integer> returnedItems);

    @PostMapping("/book")
    void bookProductsFromOrder(@RequestBody OrderDto orderDto);
}
