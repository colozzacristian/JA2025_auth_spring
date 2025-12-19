package it.eforhum.auth_module.daos;

import static java.lang.String.format;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.cdimascio.dotenv.Dotenv;
import it.eforhum.auth_module.entities.Token;

public class TokenDAO {

    private final HashMap<String, Token> tokenMap;
    private static final Logger logger = Logger.getLogger(TokenDAO.class.getName());
    private static final int TOKEN_EXPIRY_SECONDS = Integer.parseInt(Dotenv.load().get("token_expiration"));

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
        logger.info("Invalidating all tokens");
        tokenMap.clear();
    }

    public void purgeExpiredTokens() {
        for (Token token : tokenMap.values()) {
            if (token.isExpired()) {
                if(logger.isLoggable(Level.INFO)){
                    logger.log(Level.INFO, format("Purging expired token for user: %s", token.getUser().getEmail()));
                }
                tokenMap    .remove(token.getUser().getEmail());
            }
        }
    }

    public boolean isTokenValid(String email, String tokenStr) {
        Token token = tokenMap.get(email);
        if( token != null && token.getType().equals(Token.VALID_TYPES[0]) ){
            if( logger.isLoggable(Level.INFO) ){
                logger.log(Level.INFO, format("Extending expiry for token of user: %s", email));
            }
            token.extendExpiry(TOKEN_EXPIRY_SECONDS); 
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
