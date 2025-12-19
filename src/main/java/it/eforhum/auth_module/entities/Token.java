package it.eforhum.auth_module.entities;


import java.time.LocalDateTime;
import java.util.Arrays;


// the token is immutable
public class Token {
    public static final String[] VALID_TYPES = {"JWT", "OTP", "RECOVERY"};

    private final String tokenValue; 

    private LocalDateTime expiryDate;
    private User user;
    private final String type;

    public Token(String token, LocalDateTime expiryDate, User user, String type) {
        if (Arrays.stream(VALID_TYPES).noneMatch(type::equals)) {
            throw new IllegalArgumentException("Invalid token type");
            
        }
        this.tokenValue = token;
        this.expiryDate = expiryDate;
        this.user = user;
        this.type = type;
    }

    public String getTokenValue() {
        return tokenValue;
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

    public LocalDateTime extendExpiry(int seconds) {
        this.expiryDate = this.expiryDate.plusSeconds(seconds);
        return this.expiryDate;
    }


}
