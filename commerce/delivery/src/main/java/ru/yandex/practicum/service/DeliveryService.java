package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.feign.OrderOperations;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.repository.DeliveryRepository;
import ru.yandex.practicum.dto.DeliveryState;


import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final OrderOperations orderOperations;

    private static final double BASE_RATE = 5.0;
    private static final double WAREHOUSE_1_COEFF = 1.0;
    private static final double WAREHOUSE_2_COEFF = 2.0;
    private static final double FRAGILITY_COEFF = 0.2;
    private static final double WEIGHT_COEFF = 0.3;
    private static final double VOLUME_COEFF = 0.2;
    private static final double ADDRESS_COEFF = 0.2;

    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = DeliveryMapper.mapToDelivery(deliveryDto);
        return DeliveryMapper.mapToDeliveryDto(deliveryRepository.save(delivery));
    }

    public void successful(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Не найдена доставка с id " + deliveryId) );
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        orderOperations.orderDeliverySuccess(delivery.getOrderId());
    }

    public void picked(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Не найдена доставка с id " + deliveryId) );
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        //TODO: добавить WAREHOUSE
        deliveryRepository.save(delivery);
        orderOperations.orderDeliverySuccess(delivery.getOrderId());
    }

    public void failed(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Не найдена доставка с id " + deliveryId) );
        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);
        orderOperations.orderDeliveryFailed(delivery.getOrderId());
    }

    public double deliveryCostCalculation(OrderDto orderDto) {
        Delivery delivery = deliveryRepository.findById(orderDto.getDeliveryId())
                .orElseThrow(() -> new NotFoundException("Не найдена доставка с id " + orderDto.getDeliveryId()) );
        Address warehouseAddress = delivery.getFromAddress();
        Address destinationAddress = delivery.getToAddress();
        double deliveryCost = BASE_RATE;
        if (warehouseAddress.getStreet().equals("ADDRESS_1")) {
            deliveryCost += WAREHOUSE_1_COEFF * BASE_RATE;
        } else if (warehouseAddress.getStreet().equals("ADDRESS_2")) {
            deliveryCost += WAREHOUSE_2_COEFF * BASE_RATE;
        }
        if (orderDto.getFragile()) {
            deliveryCost += FRAGILITY_COEFF * deliveryCost;
        }
        deliveryCost += WEIGHT_COEFF * orderDto.getWeight();
        deliveryCost += VOLUME_COEFF * orderDto.getVolume();
        if (!destinationAddress.getStreet().equals(warehouseAddress.getStreet())) {
            deliveryCost += deliveryCost * ADDRESS_COEFF;
        }
        return deliveryCost;
    }

}
