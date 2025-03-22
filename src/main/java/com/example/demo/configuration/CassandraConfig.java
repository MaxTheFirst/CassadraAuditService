package com.example.demo.configuration;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(CassandraProperties.class)
@RequiredArgsConstructor
public class CassandraConfig {

    private final CassandraProperties properties;

    @Bean
    public CqlSession cqlSession(CqlSessionBuilder sessionBuilder) {
        InetSocketAddress address = InetSocketAddress.createUnresolved("127.0.0.1", 9042);
        sessionBuilder = sessionBuilder.addContactPoint(address);
        sessionBuilder.withKeyspace((CqlIdentifier) null);

        CqlSession session = sessionBuilder.build();

        SimpleStatement statement = SchemaBuilder.createKeyspace(properties.getKeyspaceName())
                .ifNotExists()
                .withNetworkTopologyStrategy(Map.of(properties.getLocalDatacenter(), 1))
                .build();
        session.execute(statement);

        session.execute("""
                CREATE TABLE IF NOT EXISTS my_keyspace.user_audit (
                    user_id UUID,
                    event_time TIMESTAMP,
                    event_type TEXT,
                    event_details TEXT,
                    PRIMARY KEY ((user_id), event_time)
                ) WITH CLUSTERING ORDER BY (event_time DESC)
                   AND default_time_to_live = 2592000;
                """);

        return sessionBuilder
                .withKeyspace(properties.getKeyspaceName())
                .build();
    }
}