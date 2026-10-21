package com.yoy.monitorproject.monitor;

import com.yoy.monitorproject.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class MonitorRepositoryTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private MonitorRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindSavedMonitorByName() {
        repository.saveAndFlush(new Monitor("Example", "https://example.com", CREATED_AT));
        entityManager.clear();

        assertThat(repository.findByName("Example"))
                .hasValueSatisfying(monitor -> assertThat(monitor.getUrl()).isEqualTo("https://example.com"));
    }

    @Test
    void shouldReturnEmptyWhenNameDoesNotExist() {
        assertThat(repository.findByName("Unknown")).isEmpty();
    }

    @Test
    void shouldRejectDuplicateName() {
        repository.saveAndFlush(new Monitor("Example", "https://example.com", CREATED_AT));

        assertThatThrownBy(() -> repository.saveAndFlush(new Monitor("Example", "https://other.com", CREATED_AT)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldStoreDefaultsForNewMonitor() {
        Long id = repository.saveAndFlush(new Monitor("Example", "https://example.com", CREATED_AT)).getId();
        entityManager.clear();

        Monitor loaded = repository.findById(id).orElseThrow();

        assertThat(loaded.getMethod()).isEqualTo(CheckMethod.GET);
        assertThat(loaded.getIntervalSeconds()).isEqualTo(60);
        assertThat(loaded.getTimeoutMs()).isEqualTo(5000);
        assertThat(loaded.getExpectedStatus()).isEqualTo(200);
        assertThat(loaded.isEnabled()).isTrue();
        assertThat(loaded.getStatus()).isEqualTo(MonitorStatus.UNKNOWN);
        assertThat(loaded.getLastCheckedAt()).isNull();
        assertThat(loaded.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(loaded.getUpdatedAt()).isEqualTo(CREATED_AT);
    }
}
