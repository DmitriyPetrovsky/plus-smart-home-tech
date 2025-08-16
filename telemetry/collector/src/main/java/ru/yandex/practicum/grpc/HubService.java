package ru.yandex.practicum.grpc;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.model.hub.*;
import ru.yandex.practicum.model.hub.HubEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class HubService {
    public HubEvent map(HubEventProto event) {
        HubEventProto.PayloadCase payloadCase = event.getPayloadCase();
        switch (payloadCase) {
            case DEVICE_ADDED:
                DeviceAddedEventProto deviceAddedProto = event.getDeviceAdded();
                DeviceAddedEvent deviceAdded = new DeviceAddedEvent();
                deviceAdded.setId(deviceAddedProto.getId());
                deviceAdded.setDeviceType(DeviceType.valueOf(deviceAddedProto.getType().name()));
                setCommonFields(deviceAdded, event);
                return deviceAdded;
            case DEVICE_REMOVED:
                DeviceRemovedEventProto deviceRemovedProto = event.getDeviceRemoved();
                DeviceRemovedEvent deviceRemoved = new DeviceRemovedEvent();
                deviceRemoved.setId(deviceRemovedProto.getId());
                setCommonFields(deviceRemoved, event);
                return deviceRemoved;
            case SCENARIO_ADDED:
                ScenarioAddedEventProto scenarioAddedProto = event.getScenarioAdded();
                ScenarioAddedEvent scenarioAdded = new ScenarioAddedEvent();
                scenarioAdded.setName(scenarioAddedProto.getName());
                scenarioAdded.setConditions(mapConditionsList(scenarioAddedProto.getConditionList()));
                scenarioAdded.setActions(mapActionsList(scenarioAddedProto.getActionList()));
                setCommonFields(scenarioAdded, event);
                return scenarioAdded;
            case SCENARIO_REMOVED:
                ScenarioRemovedEventProto scenarioRemovedProto = event.getScenarioRemoved();
                ScenarioRemovedEvent scenarioRemoved = new ScenarioRemovedEvent();
                scenarioRemoved.setName(scenarioRemovedProto.getName());
                setCommonFields(scenarioRemoved, event);
                return scenarioRemoved;
        }
        return null;
    }

    private <T extends HubEvent> void setCommonFields(T hubEvent, HubEventProto event) {
        hubEvent.setHubId(event.getHubId());
        hubEvent.setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()));
    }

    private List<ScenarioCondition> mapConditionsList(List<ScenarioConditionProto> conditionsProto) {
        List<ScenarioCondition> conditions = new ArrayList<>();
        for (ScenarioConditionProto conditionProto : conditionsProto) {
            ScenarioCondition condition = new ScenarioCondition();
            condition.setSensorId(conditionProto.getSensorId());
            condition.setType(ConditionType.valueOf(conditionProto.getType().name()));
            condition.setOperation(ConditionOperation.valueOf(conditionProto.getOperation().name()));
            switch (conditionProto.getValueCase()) {
                case BOOL_VALUE:
                    condition.setValue(conditionProto.getBoolValue() == true ? 1 : 0);
                    break;
                case INT_VALUE:
                    condition.setValue(conditionProto.getIntValue());
                    break;
            }
            conditions.add(condition);
        }
        return conditions;
    }

    private List<DeviceAction> mapActionsList(List<DeviceActionProto> actionsProto) {
        List<DeviceAction> actions = new ArrayList<>();
        for (DeviceActionProto actionProto : actionsProto) {
            DeviceAction action = new DeviceAction();
            action.setSensorId(actionProto.getSensorId());
            action.setType(ActionType.valueOf(actionProto.getType().name()));
            action.setValue(actionProto.getValue());
            actions.add(action);
        }
        return actions;
    }
}
