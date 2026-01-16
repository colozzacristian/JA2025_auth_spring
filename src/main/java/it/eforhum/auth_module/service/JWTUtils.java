package it.eforhum.auth_module.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import it.eforhum.auth_module.entity.Token;
import it.eforhum.auth_module.entity.User;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

@Service
public class JWTUtils {

    private final int tokenExpiration;
    private final SecretKey secretKey;

    public JWTUtils(@Value("${token.expiration}") int tokenExpiration,
                    @Value("${token.secret}") String jwtSecret) {
        this.tokenExpiration = tokenExpiration;
        this.secretKey = hmacShaKeyFor(jwtSecret.getBytes());
    }

    public Token generateJWT(User user) {
        if (!user.isActive()) {
            throw new IllegalArgumentException("Cannot generate JWT for inactive user");
        }

        Date now = Date.valueOf(LocalDate.now());
        LocalDateTime expiration = LocalDateTime.now().plusSeconds(tokenExpiration);

        String jws = Jwts.builder()
            .claim("email", user.getEmail())
            .claim("firstName", user.getFirstName())
            .claim("lastName", user.getLastName())
            .claim("userId", user.getUserId())
            .claim("groups", user.getGroupsForJWT())
            .setIssuedAt(now)
            .signWith(secretKey)
            .compact();

        return new Token(jws, expiration, user, "JWT");
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    public boolean isTokenSignatureValid(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}