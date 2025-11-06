package it.eforhum.authModule.entities;


import java.time.LocalDateTime;
import java.util.Arrays;


// the token is immutable
public class Token {
    public static final String[] VALID_TYPES = {"JWT", "OTP", "RECOVERY"};

    private final String token; 
    private final LocalDateTime expiryDate;
    private final User user;
    private final String type;

    public Token(String token, LocalDateTime expiryDate, User user, String type) {
        if (A) {
            throw new IllegalArgumentException("Invalid token type");
        }
        this.token = token;
        this.expiryDate = expiryDate;
        this.user = user;
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public User getUser() {
        if (user == null) {
            user = null; // Placeholder for actual user retrieval from jwt
        }
        return user;
    }
    public String getType() {
        return type;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }


}
