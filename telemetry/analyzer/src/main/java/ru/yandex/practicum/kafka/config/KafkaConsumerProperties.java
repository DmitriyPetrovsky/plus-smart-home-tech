package ru.yandex.practicum.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.kafka.consumer")
public class KafkaConsumerProperties {
    private String bootstrapServers;
    private boolean specificAvroReader;
    private final SnapshotProperties snapshot = new SnapshotProperties();
    private final HubEventsProperties hubevents = new HubEventsProperties();

    @Getter
    @Setter
    public static class SnapshotProperties {
        private String groupId;
        private String clientId;
        private String autoOffsetReset;
        private String keyDeserializer;
        private String valueDeserializer;
    }

    @Getter
    @Setter
    public static class HubEventsProperties {
        private String groupId;
        private String clientId;
        private String autoOffsetReset;
        private String keyDeserializer;
        private String valueDeserializer;
    }
}
