package it.eforhum.auth_module.service;

import static java.lang.String.format;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.eforhum.auth_module.entity.RateLimiting;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RateLimitingUtils {

    private final HashMap<String, RateLimiting> rateLimitingMap = new HashMap<>();
    private final List<String> whitelistIps;
    private final int timeWindowSeconds;
    private final int maxFailedRequests;

    public RateLimitingUtils(@Value("${limiting.whitelist}") String whitelistIpsConfig,
                             @Value("${limiting.backoff}") int timeWindowSeconds,
                             @Value("${limiting.maxFailedRequests}") int maxFailedRequests) {
        this.whitelistIps = whitelistIpsConfig != null && !whitelistIpsConfig.isEmpty()
            ? Arrays.asList(whitelistIpsConfig.split(","))
            : List.of();
        this.timeWindowSeconds = timeWindowSeconds;
        this.maxFailedRequests = maxFailedRequests;
    }

    public boolean isBlocked(String ip) {
        RateLimiting d = rateLimitingMap.get(ip);
        
        if (d == null || !d.excededMaxFailedRequests(maxFailedRequests)) {
            return false;
        }

        LocalDateTime blockUntil = d.getLastFailedRequest().plusSeconds(timeWindowSeconds);
        if (LocalDateTime.now().isBefore(blockUntil)) {
            if (log.isWarnEnabled()) {
                log.warn(format("IP blocked: %s", ip));
            }
            return true;
        }

        rateLimitingMap.remove(ip);
        if (log.isInfoEnabled()) {
            log.info(format("IP unblocked: %s", ip));
        }
        return false;
    }

    public void recordFailedAttempt(String ip) {
        RateLimiting d = rateLimitingMap.get(ip);
        if (d == null) {
            rateLimitingMap.put(ip, new RateLimiting());
            if (log.isDebugEnabled()) {
                log.debug(format("New rate limit entry created for IP: %s", ip));
            }
        } else {
            d.update(timeWindowSeconds);
            if (log.isDebugEnabled()) {
                log.debug(format("Failed attempt recorded for IP: %s", ip));
            }
        }
    }

    public boolean isWhitelisted(String ip) {
        return whitelistIps.contains(ip) || whitelistIps.isEmpty();
    }
}