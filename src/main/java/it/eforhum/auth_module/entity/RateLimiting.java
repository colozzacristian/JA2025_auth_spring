package it.eforhum.auth_module.entity;

import java.time.LocalDateTime;

public class RateLimiting {
    private int failedRequests;
    private LocalDateTime lastFailedRequest;

    public RateLimiting() {
        this.failedRequests = 1;
        this.lastFailedRequest = LocalDateTime.now();
    }

    public int getFailedRequests() {
        return failedRequests;
    }

    public void update(int time) {
        if(this.lastFailedRequest.plusSeconds(time).isBefore(LocalDateTime.now())) {
            this.failedRequests = 1; // reset count if outside time window
        } else {
            this.failedRequests++;
        }

        this.lastFailedRequest = LocalDateTime.now();
    }

    public boolean excededMaxFailedRequests(int maxFailedRequests) {
        return this.failedRequests >= maxFailedRequests;
    }

    public LocalDateTime getLastFailedRequest() {
        return lastFailedRequest;
    }

}
