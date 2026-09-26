package com.stocksense.common.dto;

import java.time.Instant;

public class HealthResponse {
    private String status;
    private String application;
    private Instant timestamp;

    public HealthResponse() {
    }

    public HealthResponse(String status, String application) {
        this.status = status;
        this.application = application;
        this.timestamp = Instant.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
