package com.yoy.monitorproject.monitor;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
class MonitorService {

    private final MonitorRepository monitorRepository;
    private final Clock clock;

    MonitorService(MonitorRepository monitorRepository, Clock clock) {
        this.monitorRepository = monitorRepository;
        this.clock = clock;
    }

    Monitor create(MonitorCreateRequest request) {
        String trimmedName = request.name().strip();

        if (monitorRepository.findByName(trimmedName).isPresent()) {
            throw new MonitorNameAlreadyExistsException(trimmedName);
        }

        Monitor monitor = new Monitor(trimmedName, request.url(), Instant.now(clock));
        monitor.applySettings(request.method(), request.intervalSeconds(), request.timeoutMs(),
                request.expectedStatus());
        return monitorRepository.save(monitor);
    }

    @Transactional(readOnly = true)
    Monitor getById(Long id) {
        return monitorRepository.findById(id).orElseThrow(() -> new MonitorNotFoundException(id));
    }

    @Transactional(readOnly = true)
    List<Monitor> findAll() {
        return monitorRepository.findAll();
    }

    Monitor update(Long id, MonitorUpdateRequest request) {
        Monitor monitor = getById(id);
        String trimmedName = request.name().strip();

        Monitor collision = monitorRepository.findByName(trimmedName).orElse(null);

        if (collision != null && !collision.getId().equals(monitor.getId())) {
            throw new MonitorNameAlreadyExistsException(trimmedName);
        }

        monitor.setName(trimmedName);
        monitor.setUrl(request.url());
        monitor.applySettings(request.method(), request.intervalSeconds(), request.timeoutMs(),
                request.expectedStatus());
        monitor.setUpdatedAt(Instant.now(clock));

        return monitor;
    }

    void delete(Long id) {
        Monitor monitor = getById(id);
        monitorRepository.delete(monitor);
    }

    Monitor pause(Long id) {
        Monitor monitor = getById(id);

        if (monitor.isEnabled()) {
            monitor.setEnabled(false);
            monitor.setStatus(MonitorStatus.PAUSED);

            monitor.setUpdatedAt(Instant.now(clock));
        }
        return monitor;
    }

    Monitor resume(Long id) {
        Monitor monitor = getById(id);

        if (!monitor.isEnabled()) {
            monitor.setEnabled(true);
            monitor.setStatus(MonitorStatus.UNKNOWN);

            monitor.setUpdatedAt(Instant.now(clock));
        }
        return monitor;
    }
}
