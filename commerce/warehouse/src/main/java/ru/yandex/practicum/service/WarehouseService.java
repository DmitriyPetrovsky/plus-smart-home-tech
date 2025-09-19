package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.exception.NotEnoughQuantityException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ProductAlreadyInWarehouseException;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.feign.ShoppingCartOperations;
import ru.yandex.practicum.mapper.WarehouseProductMapper;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.WarehouseRepository;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final ShoppingCartOperations shoppingCartOperations;

    private final List<AddressDto> addresses = List.of(
            new AddressDto("ADDRESS_1", "ADDRESS_1", "ADDRESS_1", "ADDRESS_1", "ADDRESS_1"),
            new AddressDto("ADDRESS_2", "ADDRESS_2", "ADDRESS_2", "ADDRESS_2", "ADDRESS_2")
    );

    @Transactional
    public void addNewProductToWarehouse(NewProductInWarehouseRequest request) {
        checkIfProductAlreadyInWarehouse(request.getProductId());
        WarehouseProduct product = WarehouseProductMapper.mapToWarehouseProduct(request);
        warehouseRepository.save(product);
    }


    private void checkIfProductAlreadyInWarehouse(UUID id) {
        warehouseRepository.findById(id)
                .ifPresent(product -> {
                    throw new ProductAlreadyInWarehouseException("Товар уже был добавлен в базу склада ранее!");
                });
    }

    @Transactional
    public void increaseProductQuantity(AddProductToWarehouseRequest request) {
        WarehouseProduct product = getWarehouseProduct(request.getProductId());
        product.setQuantity(product.getQuantity() + request.getQuantity());
        warehouseRepository.save(product);
    }

    private WarehouseProduct getWarehouseProduct(UUID id) {
        return warehouseRepository.findById(id).orElseThrow(() ->
                new ProductNotFoundException("Товар отсутствует в базе склада!")
        );
    }

    public AddressDto getWarehouseAddress() {
        int addressIndex = new Random().nextInt(addresses.size());
        return addresses.get(addressIndex);
    }

    public BookedProductsDto checkShoppingCart(ShoppingCartDto shoppingCart) {
        List<UUID> productIds = new ArrayList<>(shoppingCart.getProducts().keySet());
        Map<UUID, WarehouseProduct> products = warehouseRepository.findAllByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
        boolean hasFragile = false;
        double totalVolume = 0d;
        double totalWeight = 0d;
        for (Map.Entry<UUID, Integer> entry : shoppingCart.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Integer quantity = entry.getValue();
            WarehouseProduct product = products.get(productId);
            if (product == null) {
                throw new ProductNotFoundException("Товар с ID " + productId + " не найден на складе");
            }
            if (product.getQuantity() < quantity) {
                throw new NotEnoughQuantityException("Недостаточно товара с ID " + productId + " на складе.");
            }
            if (product.isFragile()) {
                hasFragile = true;
            }
            double productVolume = product.getHeight() * product.getWidth() * product.getDepth() * quantity;
            double productWeight = product.getWeight() * quantity;
            totalVolume += productVolume;
            totalWeight += productWeight;
        }
        return new BookedProductsDto(totalWeight, totalVolume, hasFragile);
    }

    public void returnProducts(Map<UUID, Integer> items) {
        List<AddProductToWarehouseRequest> requests = items.entrySet().stream()
                .map(entry -> new AddProductToWarehouseRequest(entry.getKey(), entry.getValue()))
                .toList();
        requests.forEach(this::increaseProductQuantity);
    }

    public void bookProductsFromOrder(OrderDto orderDto) {
        ShoppingCartDto shoppingCartDto = shoppingCartOperations.getShoppingCartById(orderDto.getShoppingCartId());
        checkShoppingCart(shoppingCartDto);
        Map<UUID, Integer> products = orderDto.getProducts();
        for (Map.Entry<UUID, Integer> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Integer quantity = entry.getValue();
            WarehouseProduct product = warehouseRepository.findById(productId)
                    .orElseThrow(() -> new NotFoundException("Товар с ID " + productId + " не найден на складе"));

            product.setQuantity(product.getQuantity() - quantity);
            warehouseRepository.save(product);
        }
    }

    }
