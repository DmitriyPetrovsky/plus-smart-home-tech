package ru.yandex.practicum.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.GrpcClient;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.mapper.HubEventMapper;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotListenerService {
    private final ScenarioRepository scenarioRepository;
    private final GrpcClient grpcClient;

    @Transactional
    @KafkaListener(
            topics = "${kafka.snapshot.topic}",
            containerFactory = "snapshotListenerContainerFactory")
    public void handleSnapshotEvent(SensorsSnapshotAvro snapshot) {
        log.info("Получен snapshot хаба {} от {}", snapshot.getHubId(), snapshot.getTimestamp());
        snapshotChecking(snapshot);
    }


    public void snapshotChecking(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        Map<String, SensorStateAvro> states = snapshot.getSensorsState();

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            log.info("Нет сценариев для хаба {}", hubId);
            return;
        }

        log.info("Получено {} сценариев для хаба {}", scenarios.size(), hubId);

        for (Scenario scenario : scenarios) {
            log.info("Проверяю сценарий '{}'", scenario.getName());

            Map<String, List<Condition>> conditionsBySensor = scenario.getConditions()
                    .entrySet()
                    .stream()
                    .collect(Collectors.groupingBy(
                            Map.Entry::getKey,
                            Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                    ));

            boolean matched = conditionsBySensor.entrySet().stream()
                    .allMatch(sensorEntry -> {
                        String sensorId = sensorEntry.getKey();
                        List<Condition> conditions = sensorEntry.getValue();
                        SensorStateAvro state = states.get(sensorId);

                        if (state == null) {
                            log.warn("Нет состояния сенсора {}", sensorId);
                            return false;
                        }

                        return conditions.stream()
                                .allMatch(condition -> {
                                    boolean result = evaluateCondition(condition, state);
                                    log.info("Условие для {}: {} {} {} → {}",
                                            sensorId,
                                            condition.getType(),
                                            condition.getOperation(),
                                            condition.getValue(),
                                            result);
                                    return result;
                                });
                    });

            if (matched) {
                log.info("Активирую сценарий '{}'", scenario.getName());
                scenario.getActions().forEach((sensorId, action) -> {
                    DeviceActionRequest message = HubEventMapper.mapToDeviceActionRequest(
                            scenario, sensorId, action
                    );
                    grpcClient.send(message);
                });
            } else {
                log.info("Сценарий '{}' не удовлетворяет условиям состояния сенсоров", scenario.getName());
            }
        }
    }


    private boolean evaluateCondition(Condition condition, SensorStateAvro state) {
        Integer actual = extractValueFromSensor(condition, state);
        Integer expected = condition.getValue();

        log.info("Проверяю условия: actual = {}, expected = {}, operation = {}", actual, expected, condition.getOperation());

        if (actual == null || expected == null) {
            log.warn("Не удалось выполнить сравнение: actual или expected = null");
            return false;
        }

        return switch (condition.getOperation()) {
            case EQUALS -> actual.equals(expected);
            case GREATER_THAN -> actual > expected;
            case LOWER_THAN -> actual < expected;
        };
    }

    private Integer extractValueFromSensor(Condition condition, SensorStateAvro state) {
        Object data = state.getData();
        switch (data.getClass().getSimpleName()) {
            case "MotionSensorAvro":
                return ((MotionSensorAvro) data).getMotion() ? 1 : 0;
            case "TemperatureSensorAvro":
                return ((TemperatureSensorAvro) data).getTemperatureC();
            case "LightSensorAvro":
                return ((LightSensorAvro) data).getLuminosity();
            case "SwitchSensorAvro":
                return ((SwitchSensorAvro) data).getState() ? 1 : 0;
            case "ClimateSensorAvro":
                ClimateSensorAvro sensor = (ClimateSensorAvro) data;
                return switch (condition.getType()) {
                    case TEMPERATURE -> sensor.getTemperatureC();
                    case HUMIDITY -> sensor.getHumidity();
                    case CO2LEVEL -> sensor.getCo2Level();
                    default -> {
                        log.warn("Неизвестный тип условия для ClimateSensorAvro: {}", condition.getType());
                        yield null;
                    }
                };
            default:
                log.warn("Неизвестный тип сенсора: {}", data.getClass().getSimpleName());
                return null;
        }
    }
}