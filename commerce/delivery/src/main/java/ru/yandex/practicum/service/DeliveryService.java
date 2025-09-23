package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.feign.OrderOperations;
import ru.yandex.practicum.feign.WarehouseOperations;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.repository.DeliveryRepository;
import ru.yandex.practicum.dto.DeliveryState;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final OrderOperations orderOperations;

    private static final BigDecimal BASE_RATE = BigDecimal.valueOf(5.0);
    private static final BigDecimal WAREHOUSE_1_COEFF = BigDecimal.valueOf(1.0);
    private static final BigDecimal WAREHOUSE_2_COEFF = BigDecimal.valueOf(2.0);
    private static final BigDecimal FRAGILITY_COEFF = BigDecimal.valueOf(0.2);
    private static final BigDecimal WEIGHT_COEFF = BigDecimal.valueOf(0.3);
    private static final BigDecimal VOLUME_COEFF = BigDecimal.valueOf(0.2);
    private static final BigDecimal ADDRESS_COEFF = BigDecimal.valueOf(0.2);
    private final WarehouseOperations warehouseOperations;

    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = DeliveryMapper.mapToDelivery(deliveryDto);
        return DeliveryMapper.mapToDeliveryDto(deliveryRepository.save(delivery));
    }

    public void successful(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Не найдена доставка с id " + deliveryId));
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        orderOperations.orderDeliverySuccess(delivery.getOrderId());
    }

    public void picked(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Не найдена доставка с id " + deliveryId));
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        ShippedToDeliveryRequest request = new ShippedToDeliveryRequest();
        request.setOrderId(delivery.getOrderId());
        request.setDeliveryId(deliveryId);
        warehouseOperations.shippedToDelivery(request);
        deliveryRepository.save(delivery);
        orderOperations.orderDelivery(delivery.getOrderId());
    }

    public void failed(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Не найдена доставка с id " + deliveryId));
        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);
        orderOperations.orderDeliveryFailed(delivery.getOrderId());
    }

    public double deliveryCostCalculation(OrderDto orderDto) {
        Delivery delivery = deliveryRepository.findById(orderDto.getDeliveryId())
                .orElseThrow(() -> new NotFoundException("Не найдена доставка с id " + orderDto.getDeliveryId()));
        Address warehouseAddress = delivery.getFromAddress();
        Address destinationAddress = delivery.getToAddress();
        BigDecimal deliveryCost = BASE_RATE;
        if (warehouseAddress.getStreet().equals("ADDRESS_1")) {
            deliveryCost = deliveryCost.add(WAREHOUSE_1_COEFF.multiply(BASE_RATE));
        } else if (warehouseAddress.getStreet().equals("ADDRESS_2")) {
            deliveryCost = deliveryCost.add(WAREHOUSE_2_COEFF.multiply(BASE_RATE));
        }
        if (orderDto.getFragile()) {
            deliveryCost = deliveryCost.add(FRAGILITY_COEFF.multiply(deliveryCost));
        }
        deliveryCost = deliveryCost.add(WEIGHT_COEFF.multiply(BigDecimal.valueOf(orderDto.getWeight())));
        deliveryCost = deliveryCost.add(VOLUME_COEFF.multiply(BigDecimal.valueOf(orderDto.getVolume())));
        if (!destinationAddress.getStreet().equals(warehouseAddress.getStreet())) {
            deliveryCost = deliveryCost.add(deliveryCost.multiply(ADDRESS_COEFF));
        }
        return deliveryCost.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}