package com.yoy.monitorproject.monitor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Optional fields may be {@code null}; the monitor then keeps its default value.
 */
public record MonitorCreateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 2048) String url,
        CheckMethod method,
        @Min(30) @Max(3600) Integer intervalSeconds,
        @Min(500) @Max(30000) Integer timeoutMs,
        Integer expectedStatus
) {
}
