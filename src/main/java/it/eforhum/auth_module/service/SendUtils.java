package it.eforhum.auth_module.service;

import java.io.IOException;
import static java.lang.String.format;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.auth_module.dto.EmailReqDTO;
import it.eforhum.auth_module.entity.Token;
import it.eforhum.auth_module.entity.User;
import it.eforhum.auth_module.repository.TokenStore;
import it.eforhum.auth_module.repository.TokenDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Service
@Slf4j
public class SendUtils {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private final String messageServiceUrl;
    private final OTPUtils otpUtils;
    private final TokenStore tokenStore;


    public SendUtils(@Value("${url.message}") String messageServiceUrl, OTPUtils otpUtils, TokenStore tokenStore) {
        this.messageServiceUrl = messageServiceUrl;
        this.otpUtils = otpUtils;
        this.tokenStore = tokenStore;
    }

   



    public int sendActivationCode(User user ,String email){

        Token otp = otpUtils.generateOTP(user);
        TokenDAO tDAO = tokenStore.getOtpTokens();
        tDAO.saveToken(otp);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(format("%s/send/%s", messageServiceUrl, "email")).toURI())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    mapper.writeValueAsString(new EmailReqDTO(
                            email,
                            "Account activation code",
                            getAuthEmailBody(otp.getTokenValue())
                        )
                    )))
                    .timeout(Duration.ofSeconds(200))
                    .build();

            HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("IOException while sending activation email", e);
            return 500;

        } catch (IOException | URISyntaxException e) {
            return 500;
        }
    }

    private  String getAuthEmailBody(String token){
        return format("""
            <html>
                <body>
                    <p>This is your account activation code</p>
                    <h1>%s</h1>
                    <p>Insert this code at: <a href="%s/activate/authenticate">this page</a></p>
                </body>
            </html>
            """, token, System.getenv("FRONTEND_SERVICE_URL"));
    }


    public int sendRecoveryEmail(User user,String email){
        Token otp = otpUtils.generateOTP(user);
        TokenDAO tDAO = tokenStore.getOtpTokens();
        tDAO.saveToken(otp);

        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(format("%s/send/%s", messageServiceUrl, "email")).toURI())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    mapper.writeValueAsString(

                        new EmailReqDTO( email,
                            "Password recovery request",
                            getRecEmailBody(otp.getTokenValue())
                    ))))
                    .timeout(Duration.ofSeconds(200))
                    .build();

                    

            HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(format("InterruptedException while sending recovery message: %s", e));
            return 500;
        } catch (IOException | URISyntaxException e) {
            log.error(format("Exception while sending recovery message: %s", e));
            return 500;
        }
    }

    private String getRecEmailBody(String t){
        return format("""
            <html>
                <body>
                    <p>This is your password recovery code</p>
                    <h1>%s</h1>
                    <p>Insert this code at: <a href="%s/recovery/authenticate">this page</a></p>
                </body>
            </html>
            """, t, System.getenv("FRONTEND_SERVICE_URL"));
    }

}   
