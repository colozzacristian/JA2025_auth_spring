package it.eforhum.auth_module.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.eforhum.auth_module.entity.Token;
import it.eforhum.auth_module.entity.User;
    
@Service    
public class OTPUtils {

    private final String otpChars;
    private final int otpLength;
    private static final SecureRandom random = new SecureRandom();

    public OTPUtils(@Value("${otp.characters:0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz}") String otpChars,
                    @Value("${otp.length:6}") int otpLength) {
        this.otpChars = otpChars;
        this.otpLength = otpLength;
    }

    public Token generateOTP(User user) {
        StringBuilder otp = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            int index = random.nextInt(otpChars.length());
            otp.append(otpChars.charAt(index));
        }
        return new Token(otp.toString(), LocalDateTime.now().plusMinutes(5), user, "OTP");
    }
}
