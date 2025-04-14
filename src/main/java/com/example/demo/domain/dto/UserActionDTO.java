package com.example.demo.domain.dto;

import com.example.demo.service.Action;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserActionDTO {
    private Long userId;
    private Instant eventTime;
    private Action eventType;
    private String eventDetails;
}
