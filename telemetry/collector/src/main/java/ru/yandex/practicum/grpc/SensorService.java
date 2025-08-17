package ru.yandex.practicum.grpc;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.model.sensor.*;
import ru.yandex.practicum.model.sensor.SensorEvent;

import java.time.Instant;

@Service
public class SensorService {
    public SensorEvent map(SensorEventProto event) {
        SensorEventProto.PayloadCase payloadCase = event.getPayloadCase();
        switch (payloadCase) {
            case LIGHT_SENSOR_EVENT:
                System.out.println("Получено событие датчика освещённости");
                LightSensorProto lightSensorProto = event.getLightSensorEvent();
                LightSensorEvent lightSensor = new LightSensorEvent();
                lightSensor.setLinkQuality(lightSensorProto.getLinkQuality());
                lightSensor.setLuminosity(lightSensorProto.getLuminosity());
                setCommonFields(lightSensor, event);
                return lightSensor;
            case CLIMATE_SENSOR_EVENT:
                System.out.println("Получено событие климатического датчика");
                ClimateSensorProto climateSensorProto = event.getClimateSensorEvent();
                ClimateSensorEvent climateSensor = new ClimateSensorEvent();
                climateSensor.setTemperatureC(climateSensorProto.getTemperatureC());
                climateSensor.setHumidity(climateSensorProto.getHumidity());
                climateSensor.setCo2Level(climateSensorProto.getCo2Level());
                setCommonFields(climateSensor, event);
                return climateSensor;
            case MOTION_SENSOR_EVENT:
                System.out.println("Получено событие датчика движения");
                MotionSensorProto motionSensorProto = event.getMotionSensorEvent();
                MotionSensorEvent motionSensor = new MotionSensorEvent();
                motionSensor.setMotion(motionSensorProto.getMotion());
                motionSensor.setLinkQuality(motionSensorProto.getLinkQuality());
                motionSensor.setVoltage(motionSensorProto.getVoltage());
                setCommonFields(motionSensor, event);
                return motionSensor;
            case SWITCH_SENSOR_EVENT:
                System.out.println("Получено событие переключателя");
                SwitchSensorProto switchSensorProto = event.getSwitchSensorEvent();
                SwitchSensorEvent switchSensor = new SwitchSensorEvent();
                switchSensor.setState(switchSensorProto.getState());
                setCommonFields(switchSensor, event);
                return switchSensor;
            case TEMPERATURE_SENSOR_EVENT:
                System.out.println("Получено сообщение от датчика температуры");
                TemperatureSensorProto temperatureSensorProto = event.getTemperatureSensorEvent();
                TemperatureSensorEvent temperatureSensor = new TemperatureSensorEvent();
                temperatureSensor.setTemperatureC(temperatureSensorProto.getTemperatureC());
                temperatureSensor.setTemperatureF(temperatureSensorProto.getTemperatureF());
                setCommonFields(temperatureSensor, event);
                return temperatureSensor;
            case PAYLOAD_NOT_SET:
                System.out.println("Тип датчика не распознан");
                break;
        }
        return null;
    }

    private <T extends SensorEvent> void setCommonFields(T sensorEvent, SensorEventProto event) {
        sensorEvent.setHubId(event.getHubId());
        sensorEvent.setId(event.getId());
        sensorEvent.setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()));
    }

}