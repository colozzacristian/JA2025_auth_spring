package it.eforhum.authModule.utils;

import java.time.LocalDateTime;
import java.util.HashMap;

import it.eforhum.authModule.entities.RateLimiting;

public class RateLimitingUtils {

    private static HashMap <String, RateLimiting> rateLimitingMap = new HashMap<>();

    public static boolean isBlocked(String ip){
        RateLimiting d = rateLimitingMap.get(ip);
        
        if( d == null ||  !d.excededMaxFailedRequests() ) return false;

        LocalDateTime blockUntil = d.getLastFailedRequest().plusSeconds(RateLimiting.TIME_WINDOW_SECONDS);
        if( LocalDateTime.now().isBefore(blockUntil) ){
            // Log blocking event
            return true;
        }

        rateLimitingMap.remove(ip);
        return false;
        // Log unblocking event
    }

    public static void recordFailedAttempt(String ip){
        RateLimiting d = rateLimitingMap.get(ip);
        if( d == null ){
            rateLimitingMap.put(ip, new RateLimiting());
        }else{
            d.update();
        }
    }
}
