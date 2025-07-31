package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.kafka.KafkaProducerService;
import ru.yandex.practicum.model.EventType;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/sensors")
    public void sensorEvent(@Valid @RequestBody SensorEvent event) {
        log.info("Получено событие датчика: " + event.getType());
        log.info(event.toString());
        kafkaProducerService.sendMessage(event, EventType.SENSOR_EVENT, event.getHubId());
    }

    @PostMapping("/hubs")
    public void hubEvent(@Valid @RequestBody HubEvent event) {
        log.info("Получено событие хаба: " + event.getType());
        log.info(event.toString());
        kafkaProducerService.sendMessage(event, EventType.HUB_EVENT, event.getHubId());
    }
}

