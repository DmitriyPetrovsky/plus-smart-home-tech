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
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;

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

    @PostConstruct
    public void init() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        config.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        this.producer = new KafkaProducer<>(config);
    }

    public void sendMessage(T event) {
        String topic;
        String key;
        SpecificRecordBase avroObj;

        if (event instanceof SensorEvent) {
            topic = "telemetry.sensors.v1";
            avroObj = SensorEventMapper.mapToAvro((SensorEvent) event);
            key = ((SensorEvent) event).getHubId();
        }
        else if (event instanceof HubEvent) {
            topic = "telemetry.hubs.v1";
            avroObj = HubEventMapper.mapToAvro((HubEvent) event);
            key = ((HubEvent) event).getHubId();
        }
        else {
            throw new IllegalArgumentException("Unsupported event type: " + event.getClass());
        }
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, key, avroObj);
        producer.send(record);
    }

    @PreDestroy
    public void close() {
        if (producer != null) {
            producer.close();
        }
    }
}