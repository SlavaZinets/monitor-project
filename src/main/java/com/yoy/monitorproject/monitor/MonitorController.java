package com.yoy.monitorproject.monitor;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/monitors")
class MonitorController {

    private final MonitorService monitorService;

    MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @PostMapping
    ResponseEntity<MonitorCreateResponse> createMonitor(@RequestBody @Valid MonitorCreateRequest request) {
        Monitor created = monitorService.create(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(MonitorCreateResponse.from(created));
    }
}
