package com.yoy.monitorproject.monitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitorServiceTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant NOW = Instant.parse("2026-01-02T12:00:00Z");

    @Mock
    private MonitorRepository repository;

    private MonitorService service;

    @BeforeEach
    void setUp() {
        service = new MonitorService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void shouldSaveNewMonitorWithCreationTime() {
        when(repository.save(any(Monitor.class))).thenAnswer(returnsFirstArg());

        Monitor created = service.create(createRequest("Example"));

        verify(repository).save(created);
        assertThat(created.getName()).isEqualTo("Example");
        assertThat(created.getUrl()).isEqualTo("https://example.com");
        assertThat(created.getCreatedAt()).isEqualTo(NOW);
        assertThat(created.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldApplyDefaultsWhenOptionalFieldsAreMissing() {
        when(repository.save(any(Monitor.class))).thenAnswer(returnsFirstArg());

        Monitor created = service.create(createRequest("Example"));

        assertThat(created.getMethod()).isEqualTo(CheckMethod.GET);
        assertThat(created.getIntervalSeconds()).isEqualTo(60);
        assertThat(created.getTimeoutMs()).isEqualTo(5000);
        assertThat(created.getExpectedStatus()).isEqualTo(200);
        assertThat(created.isEnabled()).isTrue();
        assertThat(created.getStatus()).isEqualTo(MonitorStatus.UNKNOWN);
    }

    @Test
    void shouldUseGivenCheckSettingsOnCreate() {
        when(repository.save(any(Monitor.class))).thenAnswer(returnsFirstArg());

        Monitor created = service.create(
                new MonitorCreateRequest("Example", "https://example.com", CheckMethod.HEAD, 120, 2000, 204));

        assertThat(created.getMethod()).isEqualTo(CheckMethod.HEAD);
        assertThat(created.getIntervalSeconds()).isEqualTo(120);
        assertThat(created.getTimeoutMs()).isEqualTo(2000);
        assertThat(created.getExpectedStatus()).isEqualTo(204);
    }

    @Test
    void shouldThrowWhenNameAlreadyExists() {
        when(repository.findByName("Example")).thenReturn(Optional.of(monitor(1L, "Example")));

        assertThatThrownBy(() -> service.create(createRequest("Example")))
                .isInstanceOf(MonitorNameAlreadyExistsException.class)
                .hasMessageContaining("Example");
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnMonitorById() {
        Monitor monitor = monitor(1L, "Example");
        when(repository.findById(1L)).thenReturn(Optional.of(monitor));

        assertThat(service.getById(1L)).isSameAs(monitor);
    }

    @Test
    void shouldThrowWhenMonitorNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(42L))
                .isInstanceOf(MonitorNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void shouldListAllMonitors() {
        List<Monitor> monitors = List.of(monitor(1L, "First"), monitor(2L, "Second"));
        when(repository.findAll()).thenReturn(monitors);

        assertThat(service.findAll()).containsExactlyElementsOf(monitors);
    }

    @Test
    void shouldUpdateMonitorFields() {
        when(repository.findById(1L)).thenReturn(Optional.of(monitor(1L, "Example")));

        Monitor updated = service.update(1L,
                new MonitorUpdateRequest("Renamed", "https://renamed.com", CheckMethod.HEAD, 120, 2000, 204));

        assertThat(updated.getName()).isEqualTo("Renamed");
        assertThat(updated.getUrl()).isEqualTo("https://renamed.com");
        assertThat(updated.getMethod()).isEqualTo(CheckMethod.HEAD);
        assertThat(updated.getIntervalSeconds()).isEqualTo(120);
        assertThat(updated.getTimeoutMs()).isEqualTo(2000);
        assertThat(updated.getExpectedStatus()).isEqualTo(204);
        assertThat(updated.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(updated.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldResetOptionalFieldsToDefaultsOnUpdate() {
        Monitor customised = monitor(1L, "Example");
        customised.setMethod(CheckMethod.HEAD);
        customised.setIntervalSeconds(120);
        customised.setTimeoutMs(2000);
        customised.setExpectedStatus(204);
        when(repository.findById(1L)).thenReturn(Optional.of(customised));

        Monitor updated = service.update(1L, updateRequest("Example", "https://example.com"));

        assertThat(updated.getMethod()).isEqualTo(CheckMethod.GET);
        assertThat(updated.getIntervalSeconds()).isEqualTo(60);
        assertThat(updated.getTimeoutMs()).isEqualTo(5000);
        assertThat(updated.getExpectedStatus()).isEqualTo(200);
    }

    @Test
    void shouldAllowUpdateThatKeepsOwnName() {
        Monitor monitor = monitor(1L, "Example");
        when(repository.findById(1L)).thenReturn(Optional.of(monitor));
        lenient().when(repository.findByName("Example")).thenReturn(Optional.of(monitor));

        Monitor updated = service.update(1L, updateRequest("Example", "https://new.example.com"));

        assertThat(updated.getUrl()).isEqualTo("https://new.example.com");
    }

    @Test
    void shouldThrowWhenUpdatingToNameOfAnotherMonitor() {
        when(repository.findById(1L)).thenReturn(Optional.of(monitor(1L, "Example")));
        when(repository.findByName("Taken")).thenReturn(Optional.of(monitor(2L, "Taken")));

        assertThatThrownBy(() -> service.update(1L, updateRequest("Taken", "https://example.com")))
                .isInstanceOf(MonitorNameAlreadyExistsException.class)
                .hasMessageContaining("Taken");
    }

    @Test
    void shouldThrowWhenUpdatingUnknownMonitor() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(42L, updateRequest("Example", "https://example.com")))
                .isInstanceOf(MonitorNotFoundException.class);
    }

    @Test
    void shouldDeleteMonitor() {
        Monitor monitor = monitor(1L, "Example");
        when(repository.findById(1L)).thenReturn(Optional.of(monitor));

        service.delete(1L);

        verify(repository).delete(monitor);
    }

    @Test
    void shouldThrowWhenDeletingUnknownMonitor() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(42L))
                .isInstanceOf(MonitorNotFoundException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    void shouldPauseMonitor() {
        when(repository.findById(1L)).thenReturn(Optional.of(monitor(1L, "Example")));

        Monitor paused = service.pause(1L);

        assertThat(paused.isEnabled()).isFalse();
        assertThat(paused.getStatus()).isEqualTo(MonitorStatus.PAUSED);
        assertThat(paused.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldResumePausedMonitor() {
        Monitor paused = monitor(1L, "Example");
        paused.setEnabled(false);
        paused.setStatus(MonitorStatus.PAUSED);
        when(repository.findById(1L)).thenReturn(Optional.of(paused));

        Monitor resumed = service.resume(1L);

        assertThat(resumed.isEnabled()).isTrue();
        assertThat(resumed.getStatus()).isEqualTo(MonitorStatus.UNKNOWN);
        assertThat(resumed.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldThrowWhenPausingUnknownMonitor() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.pause(42L))
                .isInstanceOf(MonitorNotFoundException.class);
    }

    @Test
    void shouldThrowWhenResumingUnknownMonitor() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resume(42L))
                .isInstanceOf(MonitorNotFoundException.class);
    }

    @Test
    void shouldNotChangeStatusWhenResumingActiveMonitor() {
        Monitor active = monitor(1L, "Example");
        active.setStatus(MonitorStatus.UP);
        when(repository.findById(1L)).thenReturn(Optional.of(active));

        Monitor resumed = service.resume(1L);

        assertThat(resumed.isEnabled()).isTrue();
        assertThat(resumed.getStatus()).isEqualTo(MonitorStatus.UP);
        assertThat(resumed.getUpdatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void shouldLeavePausedMonitorUnchangedWhenPausedAgain() {
        Monitor alreadyPaused = monitor(1L, "Example");
        alreadyPaused.setEnabled(false);
        alreadyPaused.setStatus(MonitorStatus.PAUSED);
        when(repository.findById(1L)).thenReturn(Optional.of(alreadyPaused));

        Monitor paused = service.pause(1L);

        assertThat(paused.isEnabled()).isFalse();
        assertThat(paused.getStatus()).isEqualTo(MonitorStatus.PAUSED);
        assertThat(paused.getUpdatedAt()).isEqualTo(CREATED_AT);
    }

    private static MonitorCreateRequest createRequest(String name) {
        return new MonitorCreateRequest(name, "https://example.com", null, null, null, null);
    }

    private static MonitorUpdateRequest updateRequest(String name, String url) {
        return new MonitorUpdateRequest(name, url, null, null, null, null);
    }

    private static Monitor monitor(long id, String name) {
        Monitor monitor = new Monitor(name, "https://example.com", CREATED_AT);
        ReflectionTestUtils.setField(monitor, "id", id);
        return monitor;
    }
}
