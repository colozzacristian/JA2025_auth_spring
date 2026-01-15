package it.eforhum.auth_module.repository;

import org.springframework.stereotype.Component;

@Component
public class TokenStore {
    private final TokenDAO jwtTokens;
    private final TokenDAO otpTokens; //same for password recovery requests and account activation
    private final TokenDAO recoveryTokens;

    public TokenStore() {
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

