package com.example.demo.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.cassandra")
@Data
public class CassandraProperties {
    private String contactPoints;
    private int port;
    private String keyspaceName;
    private String localDatacenter;
}
