package com.yoy.monitorproject.monitor;

import java.time.Instant;

public record MonitorCreateResponse(
        Long id,
        String name,
        String url,
        CheckMethod method,
        int intervalSeconds,
        int timeoutMs,
        int expectedStatus,
        boolean enabled,
        MonitorStatus status,
        Instant lastCheckedAt,
        Instant createdAt,
        Instant updatedAt
        ) {
    public static MonitorCreateResponse from(Monitor monitor) {
        return new MonitorCreateResponse(
                monitor.getId(),
                monitor.getName(),
                monitor.getUrl(),
                monitor.getMethod(),
                monitor.getIntervalSeconds(),
                monitor.getTimeoutMs(),
                monitor.getExpectedStatus(),
                monitor.isEnabled(),
                monitor.getStatus(),
                monitor.getLastCheckedAt(),
                monitor.getCreatedAt(),
                monitor.getUpdatedAt()
        );
    }
}
