package it.eforhum.authModule.utils;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import it.eforhum.authModule.entities.Token;
import it.eforhum.authModule.entities.User;

public class OTPUtils {
    
    private static final String OTP_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int OTP_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generates an OTP code, in production we should step up to rfc4226
     * @return the generated OTP code
     */
    public static Token generateOTP(User user) {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            int index = random.nextInt(OTP_CHARS.length());
            otp.append(OTP_CHARS.charAt(index));
        }
        return  new Token(otp.toString(),LocalDateTime.now().plusMinutes(5),user,"OTP");
    }
}
