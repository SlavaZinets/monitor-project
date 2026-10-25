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

        if (monitorRepository.findByName(request.name()).isPresent()) {
            throw new MonitorNameAlreadyExistsException(request.name());
        }

        Monitor monitor = new Monitor(request.name(), request.url(), Instant.now(clock));

        monitor.setMethod(request.method());
        monitor.setIntervalSeconds(request.intervalSeconds());
        monitor.setTimeoutMs(request.timeoutMs());
        monitor.setExpectedStatus(request.expectedStatus());
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

        Monitor collision = monitorRepository.findByName(request.name()).orElse(null);

        if (collision != null && !collision.getId().equals(monitor.getId())) {
            throw new MonitorNameAlreadyExistsException(request.name());
        }

        monitor.setName(request.name());
        monitor.setUrl(request.url());
        monitor.setMethod(request.method());
        monitor.setIntervalSeconds(request.intervalSeconds());
        monitor.setTimeoutMs(request.timeoutMs());
        monitor.setExpectedStatus(request.expectedStatus());


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
