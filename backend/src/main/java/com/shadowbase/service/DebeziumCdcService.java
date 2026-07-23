package com.shadowbase.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PreDestroy;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.format.Json;
import jakarta.annotation.PostConstruct;
import io.debezium.engine.DebeziumEngine;
import org.springframework.stereotype.Service;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.File;
import java.sql.Statement;
import java.nio.file.Files;
import java.sql.Connection;
import java.util.Properties;
import java.sql.DriverManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


@Service
public class DebeziumCdcService {

    private static final Logger log = LoggerFactory.getLogger(DebeziumCdcService.class);

    private final ProductionEnviormentService productionEnvironmentService;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final MetricsService metricsService;

    private ExecutorService executor;
    private DebeziumEngine<ChangeEvent<String, String>> engine;

    public DebeziumCdcService(ProductionEnviormentService productionEnvironmentService, KafkaEventPublisher kafkaEventPublisher, MetricsService metricsService) {
        this.productionEnvironmentService = productionEnvironmentService;
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.metricsService = metricsService;
    }

    @PostConstruct
    public void start() throws Exception {
        seedProductionSchema();

        // Where Debezium keeps its own bookkeeping files (reading position,known table structure). The system temp dir works on any OS
        File dataDir = new File(System.getProperty("java.io.tmpdir"), "shadowbase-debezium");
        if (dataDir.exists()) {
            File[] leftovers = dataDir.listFiles();
            if (leftovers != null) {
                for (File f : leftovers) {
                    f.delete();
                }
            }
        }
        Files.createDirectories(dataDir.toPath());

        PostgreSQLContainer productionDb = productionEnvironmentService.getProductionDb();

        Properties props = new Properties();
        props.setProperty("name", "production-cdc-engine");
        props.setProperty("connector.class", "io.debezium.connector.postgresql.PostgresConnector");

        // Remembers how far Debezium has read in the WAL, so a restart resumes instead of re-reading everything from the beginning
        props.setProperty("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
        props.setProperty("offset.storage.file.filename", new File(dataDir, "offsets.dat").getAbsolutePath());
        props.setProperty("offset.flush.interval.ms", "1000");

        // Remembers the table structure Debezium has seen so far.
        props.setProperty("schema.history.internal", "io.debezium.storage.file.history.FileSchemaHistory");
        props.setProperty("schema.history.internal.file.filename", new File(dataDir, "schema-history.dat").getAbsolutePath());

        // Connection details for the mock production database — read live from the container, since Testcontainers assigns a random port
        props.setProperty("database.hostname", productionDb.getHost());
        props.setProperty("database.port", String.valueOf(productionDb.getFirstMappedPort()));
        props.setProperty("database.user", productionDb.getUsername());
        props.setProperty("database.password", productionDb.getPassword());
        props.setProperty("database.dbname", productionDb.getDatabaseName());
        props.setProperty("topic.prefix", "production");

        // "pgoutput" is Postgres's built-in logical decoding plugin — no extra plugin install needed on our image
        props.setProperty("plugin.name", "pgoutput");

        // Only watch the one table we care about.
        props.setProperty("table.include.list", "public.customers");

        engine = DebeziumEngine.create(Json.class)
                .using(props)
                .notifying(this::handleChangeEvent)
                .build();

        // The engine blocks the thread it runs on, so give it its own thread
        executor = Executors.newSingleThreadExecutor();
        executor.execute(engine);

        log.info("Debezium CDC engine started — watching production.public.customers");
    }

    private void handleChangeEvent(ChangeEvent<String, String> record) {
        log.info("CDC event captured!\n  key:   {}\n  value: {}", record.key(), record.value());
        kafkaEventPublisher.publish(record.key(), record.value());
        metricsService.incrementEventsCaptured();
    }

    // Make sure the production database has a table for Debezium to watch
    private void seedProductionSchema() throws Exception {
        PostgreSQLContainer productionDb = productionEnvironmentService.getProductionDb();

        try (Connection conn = DriverManager.getConnection(
                productionDb.getJdbcUrl(), productionDb.getUsername(), productionDb.getPassword());
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    id    SERIAL PRIMARY KEY,
                    name  VARCHAR(100) NOT NULL,
                    email VARCHAR(200) NOT NULL
                )
                """);
            stmt.execute("ALTER TABLE customers REPLICA IDENTITY FULL");
            log.info("Production schema ready — 'customers' table exists with REPLICA IDENTITY FULL");
        }
    }

    @PreDestroy
    public void stop() throws Exception {
        log.info("Stopping Debezium CDC engine...");
        if (engine != null) engine.close();
        if (executor != null) executor.shutdown();
    }
}