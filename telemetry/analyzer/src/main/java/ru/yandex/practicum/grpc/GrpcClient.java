package ru.yandex.practicum.grpc;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class GrpcClient {
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub stub;

    public void send(DeviceActionRequest request) {
        try {
            stub.handleDeviceAction(request);
            log.info("Команда отправлена: \n{}", request);
        } catch (Exception e) {
            log.error("Ошибка отправки команды: {}", e.getMessage(), e);
        }
    }
}