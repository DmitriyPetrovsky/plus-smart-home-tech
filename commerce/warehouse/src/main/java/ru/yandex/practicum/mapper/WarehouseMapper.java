package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.model.OrderBooking;
import ru.yandex.practicum.model.WarehouseProduct;

public class WarehouseMapper {
    public static WarehouseProduct mapToWarehouseProduct(NewProductInWarehouseRequest request) {
        WarehouseProduct product = new WarehouseProduct();
        product.setProductId(request.getProductId());
        product.setWeight(request.getWeight());
        product.setWidth(request.getDimension().getWidth());
        product.setHeight(request.getDimension().getHeight());
        product.setDepth(request.getDimension().getDepth());
        product.setFragile(request.getFragile());
        return product;
    }

    public static OrderBooking mapToOrderBooking(BookedProductsDto bookedProductsDto,
                                                 AssemblyProductsForOrderRequest assemblyProductsForOrderRequest) {
        OrderBooking orderBooking = new OrderBooking();
        orderBooking.setFragile(bookedProductsDto.getFragile());
        orderBooking.setDeliveryWeight(bookedProductsDto.getDeliveryWeight());
        orderBooking.setDeliveryVolume(bookedProductsDto.getDeliveryVolume());
        orderBooking.setProducts(assemblyProductsForOrderRequest.getProducts());
        orderBooking.setOrderId(assemblyProductsForOrderRequest.getOrderId());
        return orderBooking;
    }
}
