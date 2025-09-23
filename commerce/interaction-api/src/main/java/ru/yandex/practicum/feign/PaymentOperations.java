package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;

import java.util.UUID;

@FeignClient(name = "payment")
public interface PaymentOperations {

    @PostMapping()
    PaymentDto createPayment(@RequestBody OrderDto orderDto);

    @PostMapping("/totalCost")
    double getTotalCost(@RequestBody OrderDto orderDto);

    @PostMapping("/refund")
    void refund(@RequestBody UUID paymentId);

    @PostMapping("/productCost")
    double productCost(@RequestBody OrderDto orderDto);

    @PostMapping("/failed")
    void failed(@RequestBody UUID paymentId);
}
