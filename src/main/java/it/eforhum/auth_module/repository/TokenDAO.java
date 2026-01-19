package it.eforhum.auth_module.repository;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import it.eforhum.auth_module.entity.Token;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class TokenDAO {

    private final HashMap<String, Token> tokenMap;

    @Value("${token.expiration}")
    private int tokenExpirySeconds;

    public TokenDAO() {
        this.tokenMap = new HashMap<>();
    }

    public void saveToken(Token token) {
        tokenMap.put(token.getUser().getEmail(), token);
    }

    public void invalidateToken(String email) {
        tokenMap.remove(email);
    }

    public void invalidateAllTokens() {
        log.info("Invalidating all tokens");
        tokenMap.clear();
    }

    public void purgeExpiredTokens() {
        for (Token token : tokenMap.values()) {
            if (token.isExpired()) {
                if(log.isInfoEnabled()){
                    log.info("Purging expired token for user: {}", token.getUser().getEmail());
                }
                tokenMap    .remove(token.getUser().getEmail());
            }
        }
    }

    public boolean isTokenValid(String email, String tokenStr) {
        Token token = tokenMap.get(email);
        if( token != null && token.getType().equals(Token.VALID_TYPES[0]) ){
            if( log.isInfoEnabled() ){
                log.info("Extending expiry for token of user: {}", email);
            }
            token.extendExpiry(tokenExpirySeconds); 
        }
        return token != null && token.getTokenValue().equals(tokenStr) && !token.isExpired();
    }
    
    public boolean isTokenValid(String tokenStr) {
        Token token = tokenMap.values().stream()
                .filter(t -> t.getTokenValue().equals(tokenStr))
                .findFirst()
                .orElse(null);
        return token != null && token.getTokenValue().equals(tokenStr) && !token.isExpired();
    }
    
}
