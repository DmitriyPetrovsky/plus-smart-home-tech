package ru.yandex.practicum.kafka.mapper;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.hub.*;

import java.util.ArrayList;
import java.util.List;

public class HubEventMapper {
    public static HubEventAvro mapToAvro(HubEvent hubEvent) {
        return HubEventAvro.newBuilder()
                .setHubId(hubEvent.getHubId())
                .setTimestamp(hubEvent.getTimestamp())
                .setPayload(mapToPayload(hubEvent))
                .build();
    }

    public static SpecificRecordBase mapToPayload(HubEvent hubEvent) {
        if (hubEvent.getType() == null) {
            throw new IllegalArgumentException("HubEvent type cannot be null");
        }
        return switch (hubEvent.getType()) {
            case DEVICE_ADDED -> {
                DeviceAddedEvent deviceAddedEvent = (DeviceAddedEvent) hubEvent;
                yield DeviceAddedEventAvro.newBuilder()
                        .setId(deviceAddedEvent.getId())
                        .setType(DeviceTypeAvro.valueOf(deviceAddedEvent.getDeviceType().name()))
                        .build();
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEvent deviceRemovedEvent = (DeviceRemovedEvent) hubEvent;
                yield DeviceRemovedEventAvro.newBuilder()
                        .setId(deviceRemovedEvent.getId())
                        .build();
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEvent scenarioAddedEvent = (ScenarioAddedEvent) hubEvent;
                yield ScenarioAddedEventAvro.newBuilder()
                        .setName(scenarioAddedEvent.getName())
                        .setConditions(mapToConditionsAvro(scenarioAddedEvent.getConditions()))
                        .setActions(mapToDeviceActionsAvro(scenarioAddedEvent.getActions()))
                        .build();
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEvent scenarioRemovedEvent = (ScenarioRemovedEvent) hubEvent;
                yield ScenarioRemovedEventAvro.newBuilder()
                        .setName(scenarioRemovedEvent.getName())
                        .build();
            }
        };
    }

    private static List<ScenarioConditionAvro> mapToConditionsAvro(List<ScenarioCondition> scenarioConditions) {
        List<ScenarioConditionAvro> conditionsAvro = new ArrayList<>();
        scenarioConditions.forEach(scenarioCondition -> {
            ScenarioConditionAvro element = ScenarioConditionAvro.newBuilder()
                    .setSensorId(scenarioCondition.getSensorId())
                    .setType(ConditionTypeAvro.valueOf(scenarioCondition.getType().name()))
                    .setOperation(ConditionOperationAvro.valueOf(scenarioCondition.getOperation().name()))
                    .setValue(scenarioCondition.getValue())
                    .build();
            conditionsAvro.add(element);
        });
        return conditionsAvro;
    }

    private static List<DeviceActionAvro> mapToDeviceActionsAvro(List<DeviceAction> deviceActions) {
        List<DeviceActionAvro> actionsAvro = new ArrayList<>();
        deviceActions.forEach(deviceAction -> {
            DeviceActionAvro element = DeviceActionAvro.newBuilder()
                    .setSensorId(deviceAction.getSensorId())
                    .setType(ActionTypeAvro.valueOf(deviceAction.getType().name()))
                    .setValue(deviceAction.getValue())
                    .build();
            actionsAvro.add(element);
        });
        return actionsAvro;
    }
}
