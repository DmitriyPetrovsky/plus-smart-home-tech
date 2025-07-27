package ru.yandex.practicum.kafka.mapper;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.sensor.*;

public class SensorEventMapper {
    public static SensorEventAvro mapToAvro(SensorEvent sensorEvent) {
        return SensorEventAvro.newBuilder()
                .setId(sensorEvent.getId())
                .setHubId(sensorEvent.getHubId())
                .setTimestamp(sensorEvent.getTimestamp())
                .setPayload(mapToPayload(sensorEvent))
                .build();
    }

    public static SpecificRecordBase mapToPayload(SensorEvent sensorEvent) {
        return switch (sensorEvent.getType()) {
            case CLIMATE_SENSOR_EVENT -> {
                ClimateSensorEvent climateEvent = (ClimateSensorEvent) sensorEvent;
                yield ClimateSensorAvro.newBuilder()
                        .setTemperatureC(climateEvent.getTemperatureC())
                        .setHumidity(climateEvent.getHumidity())
                        .setCo2Level(climateEvent.getCo2Level())
                        .build();
            }
            case LIGHT_SENSOR_EVENT -> {
                LightSensorEvent lightEvent = (LightSensorEvent) sensorEvent;
                yield LightSensorAvro.newBuilder()
                        .setLinkQuality(lightEvent.getLinkQuality())
                        .setLuminosity(lightEvent.getLuminosity())
                        .build();
            }
            case MOTION_SENSOR_EVENT -> {
                MotionSensorEvent motionEvent = (MotionSensorEvent) sensorEvent;
                yield MotionSensorAvro.newBuilder()
                        .setLinkQuality(motionEvent.getLinkQuality())
                        .setMotion(motionEvent.isMotion())
                        .setVoltage(motionEvent.getVoltage())
                        .build();
            }
            case SWITCH_SENSOR_EVENT -> {
                SwitchSensorEvent switchEvent = (SwitchSensorEvent) sensorEvent;
                yield SwitchSensorAvro.newBuilder()
                        .setState(switchEvent.isState())
                        .build();
            }
            case TEMPERATURE_SENSOR_EVENT -> {
                TemperatureSensorEvent temperatureEvent = (TemperatureSensorEvent) sensorEvent;
                yield TemperatureSensorAvro.newBuilder()
                        .setTemperatureC(temperatureEvent.getTemperatureC())
                        .setTemperatureF(temperatureEvent.getTemperatureF())
                        .build();
            }
        };
    }
}
