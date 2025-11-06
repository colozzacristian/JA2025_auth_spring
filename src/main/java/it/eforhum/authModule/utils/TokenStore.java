package it.eforhum.authModule.utils;

import it.eforhum.authModule.daos.TokenDAO;

public class TokenStore {
    private static TokenStore instance;
    private final TokenDAO jwt_token;
    private final TokenDAO otp_token; //same for password recovery requests and account activation
    private final TokenDAO recovery_token;

    public static synchronized TokenStore getInstance() {
        if (instance == null) {
            instance = new TokenStore();
        }
        return instance;
    }

    private TokenStore() {
        this.jwt_token = new TokenDAO();
        this.otp_token = new TokenDAO();
        this.recovery_token = new TokenDAO();
    }

    public TokenDAO getJwtToken() {
        return jwt_token;
    }

    public TokenDAO getOtpToken() {
        return otp_token;
    }

    public TokenDAO getRecoveryToken() {
        return recovery_token;
    }

    public void clearAllTokens() {
        jwt_token.invalidateAllTokens();
        otp_token.invalidateAllTokens();
        recovery_token.invalidateAllTokens();
    }

}

