package it.eforhum.authModule.utils;

import java.sql.Date;
import java.time.LocalDate;

import javax.crypto.SecretKey;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Jwts;
import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;
import it.eforhum.authModule.entities.Token;
import it.eforhum.authModule.entities.User;

public class JWTUtils {

    public static final int TOKEN_EXPIRATION = 30;
    private static final SecretKey SECRET_KEY;

    static {
        
        SECRET_KEY = hmacShaKeyFor(Dotenv.load().get("JWT_SECRET").getBytes());
    }
    
    public static Token generateJWT(User user) {

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
            .claim("groups", user.getGroupsForJWT())
            .setIssuedAt(now)           // Even if they are deprecated i must keep them for now
            .setExpiration(expiration)  // It seems that in this version it is not yet fully replaced
        .signWith(SECRET_KEY)
        .compact();

        return new Token(jws, expiration.toLocalDate(), user, "JWT");
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
        } catch (Exception e) {
            return false;
        }
    }
}