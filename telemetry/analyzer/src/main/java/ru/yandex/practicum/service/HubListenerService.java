package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.mapper.HubEventMapper;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.repository.SensorRepository;


@Slf4j
@Service
@RequiredArgsConstructor
public class HubListenerService {
    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;

    @KafkaListener(
            topics = "${kafka.hubevents.topic}",
            containerFactory = "hubEventListenerContainerFactory")
    public void handleHubEvent(HubEventAvro hubEvent) {
        log.info("Получено сообщение от хаба {} от {}. Начинаю обработку...", hubEvent.getHubId(), hubEvent.getTimestamp());
        hubEventHandler(hubEvent);
    }

    private void hubEventHandler(HubEventAvro hubEvent) {
        if (hubEvent.getPayload().getClass() == DeviceAddedEventAvro.class) {
            log.info("Сообщение идентифицировано как команда для добавления нового сенсора");
            addDevice(hubEvent);
        }
        if (hubEvent.getPayload().getClass() == DeviceRemovedEventAvro.class) {
            log.info("Сообщение идентифицировано как команда для удаления сенсора");
            removeDevice(hubEvent);
        }
        if (hubEvent.getPayload().getClass() == ScenarioAddedEventAvro.class) {
            log.info("Сообщение идентифицировано как команда для добавления сценария");
            addScenario(hubEvent);
        }
        if (hubEvent.getPayload().getClass() == ScenarioRemovedEventAvro.class) {
            log.info("Сообщение идентифицировано как команда для удаления сценария");
            removeScenario(hubEvent);
        }
    }

    public void addDevice(HubEventAvro hubEvent) {
        try {
            sensorRepository.save(HubEventMapper.mapToSensor(hubEvent));
        } catch (Exception e) {
            log.error("Ошибка при добавлении нового сенсора: {}", e.getMessage());
        }
    }

    public void removeDevice(HubEventAvro hubEvent) {
        try {
            sensorRepository.deleteById(HubEventMapper.mapToSensorId(hubEvent));
            log.info("Сенсор успешно удалён!");
        } catch (Exception e) {
            log.error("Ошибка при удалении сенсора: {}", e.getMessage());
        }
    }

    public void addScenario(HubEventAvro hubEvent) {
        try {
            scenarioRepository.save(HubEventMapper.mapToScenario(hubEvent));
            log.info("Сценарий успешно добавлен!");
        } catch (Exception e) {
            log.error("Ошибка при добавлении нового сценария: {}", e.getMessage());
        }
    }

    public void removeScenario(HubEventAvro hubEvent) {
        try {
            scenarioRepository.deleteByName(HubEventMapper.mapToScenarioName(hubEvent));
            log.info("Cценарий успешно удалён!");
        } catch (Exception e) {
            log.error("Ошибка при удалении сценария: {}", e.getMessage());
        }
    }
}

