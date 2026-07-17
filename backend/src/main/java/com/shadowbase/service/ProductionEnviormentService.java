package com.shadowbase.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Service
public class ProductionEnviormentService {
    private static final Logger log = LoggerFactory.getLogger(ProductionEnviormentService.class);

    private PostgreSQLContainer productionDb;
    private KafkaContainer kafka;

    @PostConstruct
    public void start() {
        log.info("Starting mock production environment (Postgres + Kafka)...");

  
        productionDb = new PostgreSQLContainer(
                DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("production")
                .withUsername("prod_user")
                .withPassword("password123")
                .withCommand(
                        "postgres",
                        "-c", "wal_level=logical",
                        "-c", "max_wal_senders=1",
                        "-c", "max_replication_slots=1"
                );
        productionDb.start();
        log.info("Production database is up on port {}", productionDb.getFirstMappedPort());

        kafka = new KafkaContainer("apache/kafka-native:3.8.0");
        kafka.start();
        log.info("Kafka broker is up at {}", kafka.getBootstrapServers());
    }

    @PreDestroy
    public void stop() {
        log.info("Shutting down mock production environment...");
        if (kafka != null) kafka.stop();
        if (productionDb != null) productionDb.stop();
    }

    public PostgreSQLContainer getProductionDb() {
        return productionDb;
    }

    public KafkaContainer getKafka() {
        return kafka;
    }
}
