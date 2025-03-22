package com.example.demo.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class UserActionDTO {
    private UUID userId;
    private Instant eventTime;
    private String eventType;
    private String eventDetails;
}
