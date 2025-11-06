package it.eforhum.authModule.daos;

import it.eforhum.authModule.entities.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TokenDAO {

    private HashMap<String, Token> token_map;

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
        token_map.clear();
    }

    public void purgeExpiredTokens() {
        List<String> expiredTokens = new ArrayList<>();
        for (Token token : token_map.values()) {
            if (token.isExpired()) {
                token_map.remove(token.getUser().getEmail());
            }
        }
    }

    public boolean isTokenValid(String email, String tokenStr) {
         Token token = token_map.get(email);
         return token != null && token.getToken().equals(tokenStr) && !token.isExpired();
    }


    
}
