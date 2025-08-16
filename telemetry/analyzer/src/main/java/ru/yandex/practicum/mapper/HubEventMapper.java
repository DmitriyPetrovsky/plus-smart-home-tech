package ru.yandex.practicum.mapper;

import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HubEventMapper {
    public static Sensor mapToSensor(HubEventAvro event) {
        DeviceAddedEventAvro deviceAddedEventAvro = (DeviceAddedEventAvro) event.getPayload();
        return new Sensor(deviceAddedEventAvro.getId(), event.getHubId());
    }

    public static String mapToSensorId(HubEventAvro event) {
        DeviceRemovedEventAvro deviceRemovedEventAvro = (DeviceRemovedEventAvro) event.getPayload();
        return deviceRemovedEventAvro.getId();
    }

    public static String mapToScenarioName(HubEventAvro event) {
        ScenarioRemovedEventAvro scenarioRemovedEventAvro = (ScenarioRemovedEventAvro) event.getPayload();
        return scenarioRemovedEventAvro.getName();
    }

    public static Scenario mapToScenario(HubEventAvro event) {
        ScenarioAddedEventAvro scenarioAvro = (ScenarioAddedEventAvro) event.getPayload();
        Scenario scenario = new Scenario();
        scenario.setHubId(event.getHubId());
        scenario.setName(scenarioAvro.getName());
        scenario.setActions(mapToActions(scenarioAvro.getActions()));
        scenario.setConditions(mapToConditions(scenarioAvro.getConditions()));
        return scenario;
    }

    private static Map<String, Action> mapToActions(List<DeviceActionAvro> actionAvros) {
        Map<String, Action> actions = new HashMap<>();
        actionAvros.forEach(actionAvro -> actions.put(actionAvro.getSensorId(), mapToAction(actionAvro)));
        return actions;

    }

    private static Action mapToAction(DeviceActionAvro actionAvro) {
        Action action = new Action();
        action.setType(actionAvro.getType().toString());
        action.setValue(actionAvro.getValue());
        return action;
    }

    private static Map<String, Condition> mapToConditions(List<ScenarioConditionAvro> conditionAvros) {
        Map<String, Condition> conditions = new HashMap<>();
        conditionAvros.forEach(conditionAvro -> conditions.put(conditionAvro.getSensorId(), mapToCondition(conditionAvro)));
        return conditions;
    }

    private static Condition mapToCondition(ScenarioConditionAvro scenarioConditionAvro) {
        Condition condition = new Condition();
        condition.setType(ConditionType.valueOf(scenarioConditionAvro.getType().toString()));
        condition.setOperation(scenarioConditionAvro.getOperation().toString());
        if (scenarioConditionAvro.getValue().getClass() == Boolean.class) {
            if ((Boolean) scenarioConditionAvro.getValue()) {
                condition.setValue(1);
            } else {
                condition.setValue(0);
            }
        } else if (scenarioConditionAvro.getValue().getClass() == Integer.class) {
            condition.setValue((Integer) scenarioConditionAvro.getValue());
        }
        return condition;
    }

    public static DeviceActionRequest mapToDeviceActionRequest(Scenario scenario, String sensorId, Action action) {
        return DeviceActionRequest.newBuilder()
                .setHubId(scenario.getHubId())
                .setScenarioName(scenario.getName())
                .setAction(mapToDeviceActionProto(sensorId, action))
                .build();
    }

    private static DeviceActionProto mapToDeviceActionProto(String sensorId, Action action) {
        return DeviceActionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(ActionTypeProto.valueOf(action.getType()))
                .build();
    }
}
