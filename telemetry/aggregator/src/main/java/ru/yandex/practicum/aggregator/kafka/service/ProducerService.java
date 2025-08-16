package ru.yandex.practicum.aggregator.kafka.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.Properties;

@Slf4j
@Service
@Getter
public class ProducerService {
    Producer<String, SpecificRecordBase> producer;
    @Value("${spring.kafka.bootstrap-servers}")
    private String server;
    @Value("${kafka.properties.producerClientId}")
    private String producerClientId;
    @Value("${kafka.properties.producerKeySerializer}")
    private String producerKeySerializer;
    @Value("${kafka.properties.producerValueSerializer}")
    private String producerValueSerializer;
    @Value("${kafka.properties.sensorSnapshotTopic}")
    private String topic;

    @PostConstruct
    public void init() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        config.put(ProducerConfig.CLIENT_ID_CONFIG, producerClientId);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, producerKeySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, producerValueSerializer);
        this.producer = new KafkaProducer<>(config);
    }

    public void sendMessage(SensorsSnapshotAvro snapshot, String key) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, key, snapshot);
        producer.send(record);
        log.info("AGGREGATOR: snapshot от {} для хаба с ID {} отправлен в топик {}",
                snapshot.getTimestamp(), snapshot.getHubId(), topic);
    }

    @PreDestroy
    public void close() {
        if (producer != null) {
            producer.close(Duration.ofSeconds(10));
        }
    }
}
