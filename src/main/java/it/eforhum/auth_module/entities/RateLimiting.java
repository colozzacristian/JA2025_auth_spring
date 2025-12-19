package it.eforhum.auth_module.entities;

import java.time.LocalDateTime;

import io.github.cdimascio.dotenv.Dotenv;

public class RateLimiting {
    private int failedRequests;
    private LocalDateTime lastFailedRequest;

    public static final int TIME_WINDOW_SECONDS = Integer.parseInt(Dotenv.load().get("backoff_time_seconds"));
    public static final int MAX_FAILED_REQUESTS = Integer.parseInt(Dotenv.load().get("max_failed_requests"));

    public RateLimiting() {
        this.failedRequests = 1;
        this.lastFailedRequest = LocalDateTime.now();
    }

    public int getFailedRequests() {
        return failedRequests;
    }

    public void update() {
        if(this.lastFailedRequest.plusSeconds(TIME_WINDOW_SECONDS).isBefore(LocalDateTime.now())) {
            this.failedRequests = 1; // reset count if outside time window
        } else {
            this.failedRequests++;
        }

        this.lastFailedRequest = LocalDateTime.now();
    }

    public boolean excededMaxFailedRequests() {
        return this.failedRequests >= MAX_FAILED_REQUESTS;
    }

    public LocalDateTime getLastFailedRequest() {
        return lastFailedRequest;
    }

}
