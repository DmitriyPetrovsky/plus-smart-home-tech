package ru.yandex.practicum.aggregator.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregationService {
    private final ProducerService producerService;
    private final ConcurrentMap<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    @KafkaListener(topics = "${kafka.sensor-events.topic}")
    public void handleSensorEvent(SensorEventAvro event) {
        try {
            validateEvent(event);
            log.debug("AGGREGATOR: Получено событие от датчика ID = {}", event.getId());

            updateState(event).ifPresent(snapshot -> {
                producerService.sendMessage(snapshot, event.getHubId());
                log.debug("AGGREGATOR: Отправлен обновленный снапшот для хаба {}", event.getHubId());
            });

        } catch (IllegalArgumentException e) {
            log.warn("Некорректное событие: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка обработки события: {}", event, e);
        }
    }

    private void validateEvent(SensorEventAvro event) {
        if (event == null) {
            throw new IllegalArgumentException("Событие не может быть null");
        }
        if (event.getHubId() == null || event.getId() == null) {
            throw new IllegalArgumentException("ID хаба и датчика обязательны");
        }
        if (event.getTimestamp() == null) {
            throw new IllegalArgumentException("Таймстемп обязателен");
        }
    }

    private Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        SensorsSnapshotAvro snapshot = snapshots.compute(event.getHubId(), (key, existing) -> {
            if (existing == null) {
                return SensorsSnapshotAvro.newBuilder()
                        .setHubId(event.getHubId())
                        .setSensorsState(new HashMap<>())
                        .setTimestamp(event.getTimestamp())
                        .build();
            }
            return updateSnapshot(existing, event);
        });
        boolean wasUpdated = !snapshot.getTimestamp().equals(event.getTimestamp());
        return wasUpdated ? Optional.of(snapshot) : Optional.empty();
    }

    private SensorsSnapshotAvro updateSnapshot(SensorsSnapshotAvro snapshot, SensorEventAvro event) {
        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());
        if (shouldUpdateState(oldState, event)) {
            SensorStateAvro newState = createNewState(event);
            Map<String, SensorStateAvro> newStates = new HashMap<>(snapshot.getSensorsState());
            newStates.put(event.getId(), newState);

            return SensorsSnapshotAvro.newBuilder(snapshot)
                    .setSensorsState(newStates)
                    .setTimestamp(event.getTimestamp())
                    .build();
        }
        return snapshot;
    }

    private boolean shouldUpdateState(SensorStateAvro oldState, SensorEventAvro event) {
        return oldState == null ||
                !(oldState.getTimestamp().isAfter(event.getTimestamp()) ||
                        oldState.getData().equals(event.getPayload()));
    }

    private SensorStateAvro createNewState(SensorEventAvro event) {
        return SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();
    }
}
