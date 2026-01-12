package it.eforhum.auth_module.utils;

import java.io.IOException;
import static java.lang.String.format;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.auth_module.dtos.EmailReqDTO;
import it.eforhum.auth_module.entities.Token;

public class SendUtils {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(SendUtils.class.getName());

    private SendUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static int sendActivationCode(Token token ,String email){
        HttpClient httpClient = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(format("%s/send/%s", System.getenv("MESSAGE_SERVICE_URL"), "email")).toURI())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    mapper.writeValueAsString(new EmailReqDTO(
                            email,
                            "Account activation code",
                            getEmailBody(token.getTokenValue())
                        )
                    )))
                    .timeout(Duration.ofSeconds(200))
                    .build();

            HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "IOException while sending activation email", e);
            return 500;

        } catch (IOException | URISyntaxException e) {
            return 500;
        }
    }

    private static  String getEmailBody(String token){
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

}   
