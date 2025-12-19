package it.eforhum.auth_module.servlets;

import java.io.IOException;
import static java.lang.String.format;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;
import it.eforhum.auth_module.daos.TokenDAO;
import it.eforhum.auth_module.daos.UserDAOImp;
import it.eforhum.auth_module.dtos.EmailReqDTO;
import it.eforhum.auth_module.dtos.RecoveryRequestDTO;
import it.eforhum.auth_module.entities.Token;
import it.eforhum.auth_module.entities.User;
import it.eforhum.auth_module.utils.OTPUtils;
import it.eforhum.auth_module.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name="ActivationReqServlet", urlPatterns="/activation")
public class ActivationReqServlet extends HttpServlet{

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Dotenv dotenv = Dotenv.load();
    private static final UserDAOImp userDAO = new UserDAOImp();
    private static final Logger logger = Logger.getLogger(ActivationReqServlet.class.getName());
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws  ServletException{
        RecoveryRequestDTO activationData;
        try {
           activationData = mapper.readValue(request.getInputStream().readAllBytes(), RecoveryRequestDTO.class);
            
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading request body", e);
            response.setStatus(400);
            return;
        }
        
        User u = userDAO.getByEmail(activationData.recepient());
        
        if(u == null){
            return;
        }

        Token otp = OTPUtils.generateOTP(u);
        TokenStore tkStore = TokenStore.getInstance();
        TokenDAO tDAO = tkStore.getOtpTokens();
        tDAO.saveToken(otp);

        int status = sendActivationCode(otp, activationData.recepient());

        if(status == 200){
            response.setStatus(200);
            return;
        }

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        
    }

    protected int sendActivationCode(Token token ,String email){
        HttpClient httpClient = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(format("%s/send/%s", dotenv.get("MESSAGE_SERVICE_URL"), "email")).toURI())
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
        } catch (Exception e) {

            return 500;
        }
    }

    private String getEmailBody(String token){
        return format("""
            <html>
                <body>
                    <p>This is your account activation code</p>
                    <h1>%s</h1>
                    <p>Insert this code at: /activate/authenticate</p>
                </body>
            </html>
            """, token);
    }

}
