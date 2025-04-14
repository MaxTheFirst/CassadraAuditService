package com.example.demo.service;

import com.datastax.oss.driver.api.core.servererrors.InvalidQueryException;
import com.example.demo.domain.dto.UserActionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserAuditServiceTest {
    private static final Duration defaultDuration = Duration.ofSeconds(5);

    @Container
    private static final CassandraContainer<?> cassandraContainer =
            new CassandraContainer<>("cassandra:4.1")
                    .withExposedPorts(9042)
                    .waitingFor(Wait.forListeningPort());
    @Autowired
    private UserAuditService userAuditService;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        String contactPoint =
                cassandraContainer.getHost() + ":" + cassandraContainer.getMappedPort(9042);
        registry.add("spring.cassandra.contact-points", () -> contactPoint);
        registry.add("spring.cassandra.local-datacenter", () -> "datacenter1");
        registry.add("spring.cassandra.keyspace-name", () -> "my_keyspace");
    }

    @Test
    void successInsertUserAction() {
        Long userId = 1L;
        Instant eventTime = Instant.now();
        String eventDetails = "User logged in";

        userAuditService.insertUserAction(
            UserActionDTO.builder()
                .userId(userId)
                .eventTime(eventTime)
                .eventType(Action.UPDATE)
                .eventDetails(eventDetails)
                .build()
        );
        List<UserActionDTO> results = userAuditService.selectUserActions(userId, eventTime, defaultDuration);

        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0).getUserId()).isEqualTo(userId);
        assertThat(results.get(0).getEventType()).isEqualTo(Action.UPDATE);
        assertThat(results.get(0).getEventDetails()).isEqualTo(eventDetails);
    }


    @Test
    void insertUserActionWithNullValuesShouldFail() {
        Long userId = null;
        Instant eventTime = Instant.now();
        String eventDetails = null;

        assertThrows(InvalidQueryException.class, () ->
                userAuditService.insertUserAction(
                    UserActionDTO.builder()
                        .userId(userId)
                        .eventTime(eventTime)
                        .eventType(Action.UPDATE)
                        .eventDetails(eventDetails)
                        .build()
                )
        );
    }

    @Test
    void selectNonExistentUserActionShouldReturnEmpty() {
        Long userId = 1234L;
        Instant eventTime = Instant.now();

        List<UserActionDTO> results = userAuditService.selectUserActions(userId, eventTime, defaultDuration);

        assertThat(results.size()).isEqualTo(0);
    }
}
