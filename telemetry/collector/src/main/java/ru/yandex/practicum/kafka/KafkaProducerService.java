package ru.yandex.practicum.kafka;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.mapper.HubEventMapper;
import ru.yandex.practicum.kafka.mapper.SensorEventMapper;
import ru.yandex.practicum.model.EventType;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Properties;

@Service
public class KafkaProducerService<T> {
    @Value("${kafka.properties.server}")
    private String server;
    @Value("${kafka.properties.clientId}")
    private String clientId;
    @Value("${kafka.properties.keySerializer}")
    private String keySerializer;
    @Value("${kafka.properties.valueSerializer}")
    private String valueSerializer;
    private Producer<String, SpecificRecordBase> producer;
    @Value("${kafka.properties.sensorEventTopic}")
    private String sensorTopic;
    @Value("${kafka.properties.hubEventTopic}")
    private String hubTopic;
    private final EnumMap<EventType, String> topics = new EnumMap<>(EventType.class);


    @PostConstruct
    public void init() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        config.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        this.producer = new KafkaProducer<>(config);
        topics.put(EventType.HUB_EVENT, hubTopic);
        topics.put(EventType.SENSOR_EVENT, sensorTopic);
    }

    public void sendMessage(T event, EventType eventType, String key) {
        String topic = topics.get(eventType);
        if (topic == null) {
            throw new IllegalArgumentException("No topic configured for event type: " + eventType);
        }
        SpecificRecordBase avroObj;
        switch (eventType) {
            case SENSOR_EVENT:
                avroObj = SensorEventMapper.mapToAvro((SensorEvent) event);
                break;
            case HUB_EVENT:
                avroObj = HubEventMapper.mapToAvro((HubEvent) event);
                break;
            default:
                throw new IllegalArgumentException("Unsupported event type: " + eventType);
        }
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, key, avroObj);
        producer.send(record);
    }

    @PreDestroy
    public void close() {
        if (producer != null) {
            producer.close(Duration.ofSeconds(10));
        }
    }
}
