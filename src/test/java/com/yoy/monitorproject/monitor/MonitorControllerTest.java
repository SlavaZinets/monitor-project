package com.yoy.monitorproject.monitor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(MonitorController.class)
class MonitorControllerTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private MonitorService service;

    @Test
    void shouldReturn201WithLocationWhenMonitorIsCreated() {
        when(service.create(any(MonitorCreateRequest.class))).thenReturn(monitor(1L, "Example"));

        MvcTestResult result = mvc.post().uri("/api/v1/monitors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Example",
                          "url": "https://example.com"
                        }
                        """)
                .exchange();

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).headers().hasValue(HttpHeaders.LOCATION, "http://localhost/api/v1/monitors/1");
    }

    @Test
    void shouldReturnMonitorJsonWhenCreated() {
        when(service.create(any(MonitorCreateRequest.class))).thenReturn(monitor(1L, "Example"));

        MvcTestResult result = mvc.post().uri("/api/v1/monitors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Example",
                          "url": "https://example.com"
                        }
                        """)
                .exchange();

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).bodyJson().isLenientlyEqualTo("""
                {
                  "id": 1,
                  "name": "Example",
                  "url": "https://example.com",
                  "method": "GET",
                  "intervalSeconds": 60,
                  "timeoutMs": 5000,
                  "expectedStatus": 200,
                  "enabled": true,
                  "status": "UNKNOWN",
                  "lastCheckedAt": null,
                  "createdAt": "2026-01-01T00:00:00Z"
                }
                """);
    }

    @Test
    void shouldReturn400WhenNameIsBlank() {
        MvcTestResult result = mvc.post().uri("/api/v1/monitors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "    ",
                          "url": "https://example.com"
                        }
                        """)
                .exchange();

        verify(service, never()).create(any());
        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnFieldErrorsWhenNameIsBlank() {
        MvcTestResult result = mvc.post().uri("/api/v1/monitors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "    ",
                          "url": "https://example.com"
                        }
                        """)
                .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().isLenientlyEqualTo("""
                {
                  "status": 400,
                  "instance": "/api/v1/monitors",
                  "errors": {
                    "name": "must not be blank"
                  }
                }
                """);
    }

    @Test
    void shouldReturn400WhenUrlIsNotHttp() {
        MvcTestResult result = mvc.post().uri("/api/v1/monitors").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Example",
                          "url": "ftp://example.com"
                        }
                        """).exchange();



        verify(service, never()).create(any());
        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);

        assertThat(result).bodyJson().isLenientlyEqualTo("""
                {
                  "status": 400,
                  "instance": "/api/v1/monitors",
                  "errors": {
                    "url": "must be a valid URL"
                  }
                }
                """);
    }


    private static Monitor monitor(long id, String name) {
        Monitor monitor = new Monitor(name, "https://example.com", CREATED_AT);
        ReflectionTestUtils.setField(monitor, "id", id);

        return monitor;
    }
}
