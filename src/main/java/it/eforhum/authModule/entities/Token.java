package it.eforhum.authModule.entities;


import java.time.LocalDateTime;

public class Token {
    private String token; 
    private LocalDateTime expiryDate;
    private User user;
    private String type;


    public Token() {
    }

    public Token(String token, LocalDateTime expiryDate, User user, String type) {
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
