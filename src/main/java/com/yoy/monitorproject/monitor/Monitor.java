package com.yoy.monitorproject.monitor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;
import java.util.Objects;

@Entity
public class Monitor {

    // Keep in sync with the column DEFAULTs in V1__create_monitor.sql
    private static final CheckMethod DEFAULT_METHOD = CheckMethod.GET;
    private static final int DEFAULT_INTERVAL_SECONDS = 60;
    private static final int DEFAULT_TIMEOUT_MS = 5000;
    private static final int DEFAULT_EXPECTED_STATUS = 200;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 2048)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CheckMethod method = DEFAULT_METHOD;

    @Column(nullable = false)
    private int intervalSeconds = DEFAULT_INTERVAL_SECONDS;

    @Column(nullable = false)
    private int timeoutMs = DEFAULT_TIMEOUT_MS;

    @Column(nullable = false)
    private int expectedStatus = DEFAULT_EXPECTED_STATUS;

    @Column(nullable = false)
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MonitorStatus status = MonitorStatus.UNKNOWN;

    private Instant lastCheckedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Monitor() {
        // for JPA
    }

    public Monitor(String name, String url, Instant createdAt) {
        this.name = name;
        this.url = url;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public CheckMethod getMethod() {
        return method;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public int getExpectedStatus() {
        return expectedStatus;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public MonitorStatus getStatus() {
        return status;
    }

    public Instant getLastCheckedAt() {
        return lastCheckedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMethod(CheckMethod method) {
        this.method = Objects.requireNonNullElse(method, DEFAULT_METHOD);
    }

    public void setIntervalSeconds(Integer intervalSeconds) {
        this.intervalSeconds = Objects.requireNonNullElse(intervalSeconds, DEFAULT_INTERVAL_SECONDS);
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = Objects.requireNonNullElse(timeoutMs, DEFAULT_TIMEOUT_MS);
    }

    public void setExpectedStatus(Integer expectedStatus) {
        this.expectedStatus = Objects.requireNonNullElse(expectedStatus, DEFAULT_EXPECTED_STATUS);
    }

    void applySettings(CheckMethod method, Integer intervalSeconds, Integer timeoutMs, Integer expectedStatus) {
        setMethod(method);
        setIntervalSeconds(intervalSeconds);
        setTimeoutMs(timeoutMs);
        setExpectedStatus(expectedStatus);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setStatus(MonitorStatus status) {
        this.status = Objects.requireNonNull(status);
    }

    public void setLastCheckedAt(Instant lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
