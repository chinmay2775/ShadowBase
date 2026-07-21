package com.shadowbase.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

@Service
public class KafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    public static final String TOPIC = "customers-changes";

    private final ProductionEnviormentService productionEnvironmentService;
    private KafkaProducer<String, String> producer;

    public KafkaEventPublisher(ProductionEnviormentService productionEnvironmentService) {
        this.productionEnvironmentService = productionEnvironmentService;
    }

    @PostConstruct
    public void start() {
        String bootstrapServers = productionEnvironmentService.getKafka().getBootstrapServers();

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<>(props);
        log.info("Kafka producer connected to {} — will publish to topic '{}'", bootstrapServers, TOPIC);
    }

    public void publish(String key, String value) {
        producer.send(new ProducerRecord<>(TOPIC, key, value), (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to publish CDC event to Kafka", exception);
            } else {
                log.info("Published CDC event to Kafka — topic={} partition={} offset={}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    @PreDestroy
    public void stop() {
        if (producer != null) {
            producer.flush();
            producer.close();
        }
    }
}