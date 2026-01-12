package it.eforhum.auth_module.utils;

import static java.lang.String.format;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.eforhum.auth_module.entities.RateLimiting;

public class RateLimitingUtils {

    private static final Logger logger = Logger.getLogger(RateLimitingUtils.class.getName());
    private static final HashMap <String, RateLimiting> rateLimitingMap = new HashMap<>();
    private static final List<String> whitelistIps = 
        List.of(System.getenv("whitelist_ips") != null ? System.getenv("whitelist_ips").split(",") : new String[0]);

    private RateLimitingUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isBlocked(String ip){
        RateLimiting d = rateLimitingMap.get(ip);
        
        if( d == null ||  !d.excededMaxFailedRequests() ) return false;

        LocalDateTime blockUntil = d.getLastFailedRequest().plusSeconds(RateLimiting.TIME_WINDOW_SECONDS);
        if( LocalDateTime.now().isBefore(blockUntil) ){
            if( logger.isLoggable(Level.WARNING) ){
                logger.log(java.util.logging.Level.WARNING, format("IP blocked: %s", ip));
            }
            return true;
        }


        rateLimitingMap.remove(ip);
        if( logger.isLoggable(Level.INFO) ){
            logger.log(Level.INFO, format("IP unblocked: %s", ip));
        }
        return false;
    }

    public static void recordFailedAttempt(String ip){
        RateLimiting d = rateLimitingMap.get(ip);
        if( d == null ){
            rateLimitingMap.put(ip, new RateLimiting());
            if (logger.isLoggable(Level.FINE)){
                logger.log(Level.FINE, format("New rate limit entry created for IP: %s", ip));
            }
        }else{
            d.update();
            if (logger.isLoggable(Level.FINE)){
                logger.log(Level.FINE, format("Failed attempt recorded for IP: %s", ip));
            }
            
        }
    }

    public static boolean isWhitelisted(String ip){
        return whitelistIps.contains(ip) || whitelistIps.isEmpty();
    }
}
