package it.eforhum.auth_module.utils;

import it.eforhum.auth_module.daos.TokenDAO;

public class TokenStore {
    private static TokenStore instance;
    private final TokenDAO jwtTokens;
    private final TokenDAO otpTokens; //same for password recovery requests and account activation
    private final TokenDAO recoveryTokens;

    public static synchronized TokenStore getInstance() {
        if (instance == null) {
            instance = new TokenStore();
        }
        return instance;
    }

    private TokenStore() {
        this.jwtTokens = new TokenDAO();
        this.otpTokens = new TokenDAO();
        this.recoveryTokens = new TokenDAO();
    }

    public TokenDAO getJwtTokens() {
        return jwtTokens;
    }

    public TokenDAO getOtpTokens() {
        return otpTokens;
    }

    public TokenDAO getRecoveryTokens() {
        return recoveryTokens;
    }

    public void clearAllTokens() {
        jwtTokens.invalidateAllTokens();
        otpTokens.invalidateAllTokens();
        recoveryTokens.invalidateAllTokens();
    }

}

