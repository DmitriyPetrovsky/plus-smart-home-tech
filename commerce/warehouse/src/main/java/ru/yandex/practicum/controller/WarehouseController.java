package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.feign.WarehouseOperations;
import ru.yandex.practicum.service.WarehouseService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/warehouse")
public class WarehouseController implements WarehouseOperations {
    private final WarehouseService warehouseService;

    @Override
    public void addProductToWarehouse(@Valid @RequestBody NewProductInWarehouseRequest request) {
        log.info("Получен запрос на добавление товара {} в базу", request.getProductId());
        warehouseService.addNewProductToWarehouse(request);
    }

    @Override
    public BookedProductsDto checkShoppingCart(ShoppingCartDto shoppingCartDto) {
        log.info("Получен запрос на проверку корзины товаров c ID: {}", shoppingCartDto.getShoppingCartId());
        return warehouseService.checkShoppingCart(shoppingCartDto);
    }

    @Override
    public void increaseProductQuantity(AddProductToWarehouseRequest request) {
        log.info("Получен запрос на увеличение количества товара с ID: {} на {} единиц", request.getProductId(), request.getQuantity());
        warehouseService.increaseProductQuantity(request);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        log.info("Получен запрос на выдачу адреса склада");
        return warehouseService.getWarehouseAddress();
    }

}
