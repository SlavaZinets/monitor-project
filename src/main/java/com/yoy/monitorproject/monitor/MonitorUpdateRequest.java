package com.yoy.monitorproject.monitor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Full replacement (PUT): optional fields that are {@code null} are reset to their default value.
 * {@code enabled} is not part of the update; it changes only through pause and resume.
 */
public record MonitorUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 2048) String url,
        CheckMethod method,
        @Min(30) @Max(3600) Integer intervalSeconds,
        @Min(500) @Max(30000) Integer timeoutMs,
        Integer expectedStatus
) {
}
