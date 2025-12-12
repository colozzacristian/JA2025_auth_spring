package it.eforhum.authModule.daos;

import static java.lang.String.format;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.cdimascio.dotenv.Dotenv;
import it.eforhum.authModule.entities.Token;

public class TokenDAO {

    private HashMap<String, Token> token_map;
    private static final Logger logger = Logger.getLogger(TokenDAO.class.getName());
    private static final int TOKEN_EXPIRY_SECONDS = Integer.parseInt(Dotenv.load().get("token_expiration"));

    public TokenDAO() {
        this.token_map = new HashMap<>();
    }

    public void saveToken(Token token) {
        token_map.put(token.getUser().getEmail(), token);
    }

    public void invalidateToken(String email) {
        token_map.remove(email);
    }

    public void invalidateAllTokens() {
        logger.info("Invalidating all tokens");
        token_map.clear();
    }

    public void purgeExpiredTokens() {
        for (Token token : token_map.values()) {
            if (token.isExpired()) {
                logger.log(Level.INFO, format("Purging expired token for user: %s", token.getUser().getEmail()));
                token_map.remove(token.getUser().getEmail());
            }
        }
    }

    public boolean isTokenValid(String email, String tokenStr) {
        Token token = token_map.get(email);
        if( token != null && token.getType().equals(Token.VALID_TYPES[0]) ){
            logger.log(Level.INFO, format("Extending expiry for token of user: %s", email));
            token.extendExpiry(TOKEN_EXPIRY_SECONDS); 
        }
        return token != null && token.getToken().equals(tokenStr) && !token.isExpired();
    }
    
    public boolean isTokenValid(String tokenStr) {
        Token token = token_map.values().stream()
                .filter(t -> t.getToken().equals(tokenStr))
                .findFirst()
                .orElse(null);
        return token != null && token.getToken().equals(tokenStr) && !token.isExpired();
    }
    
}
