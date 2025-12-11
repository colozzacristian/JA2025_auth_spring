package it.eforhum.authModule.servlets;

import java.io.IOException;
import static java.lang.String.format;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;
import it.eforhum.authModule.daos.UserDAOImp;
import it.eforhum.authModule.dtos.RecoveryMsgReqDTO;
import it.eforhum.authModule.dtos.RecoveryReqDTO;
import it.eforhum.authModule.entities.Token;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.OTPUtils;
import it.eforhum.authModule.utils.RateLimitingUtils;
import it.eforhum.authModule.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@WebServlet("/recovery")
public class PasswordRecoveryReqServlet extends HttpServlet{

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserDAOImp userDAO = new UserDAOImp();
    private final TokenStore tokenStore = TokenStore.getInstance();
    private static final List<String> allowedChannels = List.of("email"); 
    private static final Dotenv dotenv = Dotenv.load();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Logger logger = Logger.getLogger(PasswordRecoveryReqServlet.class.getName());
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        
        RecoveryReqDTO recoveryDTO = parseRequest(req, resp);
        if(recoveryDTO == null) {
            return;
        }

        User u = findUserByContact(recoveryDTO.channel(), recoveryDTO.contact());
        
        if(u == null) {
            logger.log(Level.WARNING, format("Password recovery attempt for non-existent user/channel via %s: %s from IP: %s", recoveryDTO.channel(), recoveryDTO.contact(), req.getRemoteAddr()));
            RateLimitingUtils.recordFailedAttempt(req.getRemoteAddr());
            return;
        }
        
        Token t = OTPUtils.generateOTP(u);
        tokenStore.getOtpToken().saveToken(t);
        
        int status = sendRecoveryEmail(recoveryDTO,t, u.getEmail());

        if(status == 200) {
            logger.log(Level.INFO, format("Sent recovery message to user: %s via %s", u.getEmail(), recoveryDTO.channel()));
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        logger.log(Level.SEVERE, format("Failed to send recovery message, status code: %d", status));
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.getWriter().write("Error sending recovery message");
  
    }

    private User findUserByContact(String channel, String contact) {
        User u;
        u = switch (channel) {
            case "email" -> userDAO.getByEmail(contact);
            default -> null;
        };
        return u;
    }

    private RecoveryReqDTO parseRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RecoveryReqDTO recoveryDTO;
        try{
            recoveryDTO = objectMapper.readValue(req.getInputStream(), RecoveryReqDTO.class);
        }catch(IOException e){
            logger.log(Level.WARNING, format("Failed to parse recovery request: %s", e.getMessage()));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        if(!allowedChannels.contains(recoveryDTO.channel())) {
            logger.log(Level.WARNING, format("Unsupported recovery channel used from IP: %s", req.getRemoteAddr()));
            resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            resp.getWriter().write("Channel not supported");
            return null;
        }

        return recoveryDTO;
    }

    private int sendRecoveryEmail(RecoveryReqDTO recoveryDTO, Token t,String email){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(format("%s/send/%s", dotenv.get("MESSAGE_SERVICE_URL"), recoveryDTO.channel())).toURI())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    objectMapper.writeValueAsString(
                        new RecoveryMsgReqDTO(
                            email,
                            recoveryDTO.contact(),
                            t.getToken()
                    ))))
                    .timeout(Duration.ofSeconds(200))
                    .build();
            HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
            return response.statusCode();

        } catch (IOException | InterruptedException | URISyntaxException e) {
            logger.log(Level.SEVERE, format("Exception while sending recovery message: %s", e.getMessage()));
            return 500;
        }
    }
    
}
