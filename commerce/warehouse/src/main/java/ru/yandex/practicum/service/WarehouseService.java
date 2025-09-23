package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.exception.NotEnoughQuantityException;
import ru.yandex.practicum.exception.ProductAlreadyInWarehouseException;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.OrderBooking;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.OrderBookingRepository;
import ru.yandex.practicum.repository.WarehouseRepository;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final OrderBookingRepository orderBookingRepository;

    private final List<AddressDto> addresses = List.of(
            new AddressDto("ADDRESS_1", "ADDRESS_1", "ADDRESS_1", "ADDRESS_1", "ADDRESS_1"),
            new AddressDto("ADDRESS_2", "ADDRESS_2", "ADDRESS_2", "ADDRESS_2", "ADDRESS_2")
    );

    @Transactional
    public void addNewProductToWarehouse(NewProductInWarehouseRequest request) {
        checkIfProductAlreadyInWarehouse(request.getProductId());
        WarehouseProduct product = WarehouseMapper.mapToWarehouseProduct(request);
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

    public void increaseProductsQuantity(List<AddProductToWarehouseRequest> requests) {
        List<UUID> productIds = requests.stream()
                .map(AddProductToWarehouseRequest::getProductId)
                .collect(Collectors.toList());
        Map<UUID, WarehouseProduct> products = warehouseRepository.findAllByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
        for (AddProductToWarehouseRequest request : requests) {
            WarehouseProduct product = products.get(request.getProductId());
            if (product == null) {
                throw new ProductNotFoundException("Товар с ID " + request.getProductId() + " не найден на складе");
            }
            product.setQuantity(product.getQuantity() + request.getQuantity());
        }
        warehouseRepository.saveAll(products.values());
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
        if (shoppingCart.getProducts().isEmpty()) {
            return new BookedProductsDto(0d, 0d, false);
        }
        List<UUID> productIds = new ArrayList<>(shoppingCart.getProducts().keySet());
        Map<UUID, WarehouseProduct> products = warehouseRepository.findAllByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
        return calculateBookingParameters(shoppingCart.getProducts(), products);
    }

    public void returnProducts(Map<UUID, Integer> items) {
        if (items.isEmpty()) return;
        List<AddProductToWarehouseRequest> requests = items.entrySet().stream()
                .map(entry -> new AddProductToWarehouseRequest(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        increaseProductsQuantity(requests);
    }

    private BookedProductsDto checkAndReserveProducts(AssemblyProductsForOrderRequest assembly) {
        List<UUID> productIds = new ArrayList<>(assembly.getProducts().keySet());
        Map<UUID, WarehouseProduct> products = warehouseRepository.findAllByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
        BookedProductsDto bookedProductsDto = calculateBookingParameters(assembly.getProducts(), products);
        for (Map.Entry<UUID, Integer> entry : assembly.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Integer quantity = entry.getValue();
            WarehouseProduct product = products.get(productId);
            if (product == null) {
                throw new ProductNotFoundException("Товар с ID " + productId + " не найден на складе");
            }
            if (product.getQuantity() < quantity) {
                throw new NotEnoughQuantityException("Недостаточно товара с ID " + productId + " на складе.");
            }
            product.setQuantity(product.getQuantity() - quantity);
        }
        warehouseRepository.saveAll(products.values());
        return bookedProductsDto;
    }

    @Transactional
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest assembly) {
        BookedProductsDto bookedProductsDto = checkAndReserveProducts(assembly);
        OrderBooking orderBooking = new OrderBooking();
        orderBooking.setFragile(bookedProductsDto.getFragile());
        orderBooking.setDeliveryVolume(bookedProductsDto.getDeliveryVolume());
        orderBooking.setDeliveryWeight(bookedProductsDto.getDeliveryWeight());
        orderBooking.setProducts(new HashMap<>(assembly.getProducts()));
        orderBooking.setOrderId(assembly.getOrderId());
        orderBookingRepository.save(orderBooking);
        return bookedProductsDto;
    }

    private BookedProductsDto calculateBookingParameters(Map<UUID, Integer> requestedProducts, Map<UUID, WarehouseProduct> availableProducts) {
        boolean hasFragile = false;
        double totalVolume = 0d;
        double totalWeight = 0d;
        for (Map.Entry<UUID, Integer> entry : requestedProducts.entrySet()) {
            UUID productId = entry.getKey();
            Integer quantity = entry.getValue();
            WarehouseProduct product = availableProducts.get(productId);
            if (product == null) {
                throw new ProductNotFoundException("Товар с ID " + productId + " не найден на складе");
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

    public void shipToDelivery(ShippedToDeliveryRequest request) {
        OrderBooking booking = orderBookingRepository.findByOrderId(request.getOrderId());
        if (booking != null) {
            booking.setDeliveryId(request.getDeliveryId());
            orderBookingRepository.save(booking);
        }
    }
}
