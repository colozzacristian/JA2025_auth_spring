package it.eforhum.authModule.utils;

import java.time.LocalDate;
import java.sql.Date;

import javax.crypto.SecretKey;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

import io.jsonwebtoken.Jwts;
import it.eforhum.authModule.entities.User;

public class JWTUtils {

    public static final int TOKEN_EXPIRATION = 30;
    private static final SecretKey SECRET_KEY;

    static {

        SECRET_KEY = hmacShaKeyFor(System.getenv("JWT_SECRET").getBytes());
    }
    
    public static String generateJWT(User user) {

        if (! user.isActive()) {
            throw new IllegalArgumentException("Cannot generate JWT for inactive user");
        }

        Date now = Date.valueOf(LocalDate.now());
        Date expiration = Date.valueOf(LocalDate.now().plusDays(TOKEN_EXPIRATION));
        String[]roles = {"USER","ADMIN"};

        String jws = Jwts.builder()
        .claim("email", user.getEmail())
        .claim("firstName", user.getFirstName())
        .claim("lastName", user.getLastName())
        .claim("userId", user.getUserId())
        .claim("groups", roles) // Placeholder for actual groups (Waiting for Backoffice)
        .setExpiration(expiration)
        .setIssuedAt(now)
        .signWith(SECRET_KEY)
        .compact();

        return jws;
    }
}
