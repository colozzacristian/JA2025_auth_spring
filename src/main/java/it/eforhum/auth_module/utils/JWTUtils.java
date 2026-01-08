package it.eforhum.auth_module.utils;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.crypto.SecretKey;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import it.eforhum.auth_module.entities.Token;
import it.eforhum.auth_module.entities.User;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

public class JWTUtils {

    public static final int TOKEN_EXPIRATION = Integer.parseInt(System.getenv("token_expiration"));
    private static final SecretKey SECRET_KEY;

    static {
        
        SECRET_KEY = hmacShaKeyFor(System.getenv("JWT_SECRET").getBytes());
    }

    private JWTUtils() {
        throw new IllegalStateException("Utility class");
    }
    
    
    public static Token generateJWT(User user) {

        if (! user.isActive()) {
            throw new IllegalArgumentException("Cannot generate JWT for inactive user");
        }

        Date now = Date.valueOf(LocalDate.now());
        LocalDateTime expiration = LocalDateTime.now().plusSeconds(TOKEN_EXPIRATION);

        
        String jws = Jwts.builder()
            .claim("email", user.getEmail())
            .claim("firstName", user.getFirstName())
            .claim("lastName", user.getLastName())
            .claim("userId", user.getUserId())
            .claim("groups", user.getGroupsForJWT())
            .setIssuedAt(now)           // Even if they are deprecated i must keep them for now
        .signWith(SECRET_KEY)
        .compact();

        return new Token(jws, expiration, user, "JWT");
    }


    public static String getEmailFromToken(String token) {
        return Jwts.parser().verifyWith(SECRET_KEY).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    public static boolean isTokenSignatureValid(String token) {
        try {
            Jwts.parser().verifyWith(SECRET_KEY).build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}