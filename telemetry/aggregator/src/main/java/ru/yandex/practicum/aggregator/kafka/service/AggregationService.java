package ru.yandex.practicum.aggregator.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregationService {
    private final ProducerService producerService;
    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    @KafkaListener(topics = "${kafka.sensor-events.topic}")
    public void handleSensorEvent(SensorEventAvro event) {
        try {
            log.info("AGGREGATOR: Получено событие от датчика ID = {}", event.getId());
            updateState(event)
                    .ifPresent(snapshot -> producerService.sendMessage(snapshot, event.getHubId()));
        } catch (Exception e) {
            log.error("Ошибка обработки события: {}", event, e);
        }
    }

    private Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        snapshots.computeIfAbsent(event.getHubId(),
                k -> SensorsSnapshotAvro.newBuilder()
                        .setHubId(event.getHubId())
                        .setTimestamp(Instant.now())
                        .setSensorsState(new HashMap<>())
                        .build());
        SensorsSnapshotAvro snapshot = snapshots.get(event.getHubId());
        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());
        if (oldState != null) {
            if (oldState.getTimestamp().isAfter(event.getTimestamp()) ||
                    oldState.getData().equals(event.getPayload())) {
                log.info("AGGREGATOR: snapshot не требует обновления.");
                return Optional.empty();
            }
        }
        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();
        snapshot.getSensorsState().put(event.getId(), newState);
        snapshot.setTimestamp(event.getTimestamp());
        log.info("AGGREGATOR: snapshot был обновлён.");
        return Optional.of(snapshot);
    }
}
