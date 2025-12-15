package it.eforhum.authModule.utils;

import static java.lang.String.format;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.eforhum.authModule.entities.RateLimiting;

public class RateLimitingUtils {

    private static final Logger logger = Logger.getLogger(RateLimitingUtils.class.getName());
    private static HashMap <String, RateLimiting> rateLimitingMap = new HashMap<>();

    public static boolean isBlocked(String ip){
        RateLimiting d = rateLimitingMap.get(ip);
        
        if( d == null ||  !d.excededMaxFailedRequests() ) return false;

        LocalDateTime blockUntil = d.getLastFailedRequest().plusSeconds(RateLimiting.TIME_WINDOW_SECONDS);
        if( LocalDateTime.now().isBefore(blockUntil) ){
            logger.log(java.util.logging.Level.WARNING, format("IP blocked: %s", ip));
            return true;
        }

        rateLimitingMap.remove(ip);
        logger.log(Level.INFO, format("IP unblocked: %s", ip));
        return false;
    }

    public static void recordFailedAttempt(String ip){
        RateLimiting d = rateLimitingMap.get(ip);
        if( d == null ){
            rateLimitingMap.put(ip, new RateLimiting());
            logger.log(Level.FINE, format("New rate limit entry created for IP: %s", ip));
        }else{
            d.update();
            logger.log(Level.FINE, format("Failed attempt recorded for IP: %s", ip));
        }
    }
}
