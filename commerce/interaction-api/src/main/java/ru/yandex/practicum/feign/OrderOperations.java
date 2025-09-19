package ru.yandex.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "order")
public interface OrderOperations {

    @GetMapping
    List<OrderDto> getUserOrders(@RequestParam String username);

    @PutMapping
    OrderDto createNewOrder(@Valid @RequestBody CreateNewOrderRequest request);

    @PostMapping("/return")
    OrderDto returnProducts(@Valid @RequestBody ProductReturnRequest request);

    @PostMapping("/payment")
    OrderDto orderPayment(@RequestBody UUID orderId);

    @PostMapping("/payment/failed")
    OrderDto orderPaymentFailed(@RequestBody UUID orderId);

    @PostMapping("/delivery")
    OrderDto orderDelivery(@RequestBody UUID orderId);

    @PostMapping("/delivery/failed")
    OrderDto orderDeliveryFailed(@RequestBody UUID orderId);

    @PostMapping("/completed")
    OrderDto orderDeliverySuccess(@RequestBody UUID orderId);

    @PostMapping("/calculate/total")
    OrderDto calculateTotalOrderCost(@RequestBody UUID orderId);

    @PostMapping("/calculate/products")
    OrderDto calculateProductsOrderCost(@RequestBody UUID orderId);

    @PostMapping("/calculate/delivery")
    OrderDto calculateDeliveryCost(@RequestBody UUID orderId);

    @PostMapping("/assembly")
    OrderDto orderAssembly(@RequestBody UUID orderId);

    @PostMapping("/assembly/failed")
    OrderDto orderAssemblyFailed(@RequestBody UUID orderId);
}
