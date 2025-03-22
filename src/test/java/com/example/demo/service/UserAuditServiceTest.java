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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserAuditServiceTest {

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
        UUID userId = UUID.randomUUID();
        Instant eventTime = Instant.now();
        String eventDetails = "User logged in";

        userAuditService.insertUserAction(userId, eventTime, UserAuditService.Action.INSERT, eventDetails);
        Optional<UserActionDTO> result = userAuditService.selectUserAction(userId, eventTime);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(userId);
        assertThat(result.get().getEventType()).isEqualTo(UserAuditService.Action.INSERT.toString());
        assertThat(result.get().getEventDetails()).isEqualTo(eventDetails);
    }


    @Test
    void insertUserActionWithNullValuesShouldFail() {
        UUID userId = null;
        Instant eventTime = Instant.now();
        String eventDetails = null;

        assertThrows(InvalidQueryException.class, () ->
                userAuditService.insertUserAction(userId, eventTime, UserAuditService.Action.INSERT, eventDetails)
        );
    }

    @Test
    void selectNonExistentUserActionShouldReturnEmpty() {
        UUID userId = UUID.randomUUID();
        Instant eventTime = Instant.now();

        Optional<UserActionDTO> result = userAuditService.selectUserAction(userId, eventTime);

        assertThat(result).isNotPresent();
    }
}
