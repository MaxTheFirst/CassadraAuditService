package com.example.demo.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "spring.cassandra")
@Data
public class CassandraProperties {
    private List<String> contactPoints;
    private int port;
    private String keyspaceName;
    private String localDatacenter;
}
