package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;

public class DeliveryMapper {
    public static DeliveryDto mapToDeliveryDto(Delivery delivery) {
        if (delivery == null) {
            return null;
        }

        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setDeliveryId(delivery.getDeliveryId());
        deliveryDto.setFromAddress(mapToAddressDto(delivery.getFromAddress()));
        deliveryDto.setToAddress(mapToAddressDto(delivery.getToAddress()));
        deliveryDto.setOrderId(delivery.getOrderId());
        deliveryDto.setDeliveryState(delivery.getDeliveryState());

        return deliveryDto;
    }

    public static Delivery mapToDelivery(DeliveryDto deliveryDto) {
        if (deliveryDto == null) {
            return null;
        }

        Delivery delivery = new Delivery();
        delivery.setDeliveryId(deliveryDto.getDeliveryId());
        delivery.setFromAddress(mapToAddress(deliveryDto.getFromAddress()));
        delivery.setToAddress(mapToAddress(deliveryDto.getToAddress()));
        delivery.setOrderId(deliveryDto.getOrderId());
        delivery.setDeliveryState(deliveryDto.getDeliveryState());

        return delivery;
    }

    private static AddressDto mapToAddressDto(Address address) {
        if (address == null) {
            return null;
        }

        AddressDto addressDto = new AddressDto();
        addressDto.setCountry(address.getCountry());
        addressDto.setCity(address.getCity());
        addressDto.setStreet(address.getStreet());
        addressDto.setHouse(address.getHouse());
        addressDto.setFlat(address.getFlat());

        return addressDto;
    }

    private static Address mapToAddress(AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }

        Address address = new Address();
        address.setCountry(addressDto.getCountry());
        address.setCity(addressDto.getCity());
        address.setStreet(addressDto.getStreet());
        address.setHouse(addressDto.getHouse());
        address.setFlat(addressDto.getFlat());

        return address;
    }
}
