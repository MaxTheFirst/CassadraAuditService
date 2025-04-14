package com.example.demo.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.example.demo.domain.dto.UserActionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserAuditService {

    @Autowired
    private CqlSession session;

    public void insertUserAction(UserActionDTO userAction) {
        PreparedStatement preparedStatement = session.prepare(
                "INSERT INTO my_keyspace.user_audit (user_id, event_time, event_type, event_details) " +
                        "VALUES (?, ?, ?, ?)"
        );

        BoundStatement boundStatement = preparedStatement.bind(
            userAction.getUserId(),
            userAction.getEventTime(),
            userAction.getEventType().toString(),
            userAction.getEventDetails()
        );

        session.execute(boundStatement);
    }

    public List<UserActionDTO> selectUserActions(Long userId, Instant approximateTime, Duration tolerance) {
        Instant startTime = approximateTime.minus(tolerance);
        Instant endTime = approximateTime.plus(tolerance);

        PreparedStatement preparedStatement = session.prepare(
            "SELECT user_id, event_time, event_type, event_details " +
                "FROM my_keyspace.user_audit " +
                "WHERE user_id = ? AND event_time >= ? AND event_time <= ?"
        );

        BoundStatement boundStatement = preparedStatement.bind(userId, startTime, endTime);
        ResultSet resultSet = session.execute(boundStatement);

        List<UserActionDTO> actions = new ArrayList<>();
        for (Row row : resultSet) {
            UserActionDTO action = UserActionDTO.builder()
                .userId(row.getLong("user_id"))
                .eventTime(row.getInstant("event_time"))
                .eventType(Action.valueOf(row.getString("event_type")))
                .eventDetails(row.getString("event_details"))
                .build();
            actions.add(action);
        }

        return actions;
    }
}
