package it.eforhum.auth_module.servlets;

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


@WebServlet("/recovery")
public class PasswordRecoveryReqServlet extends HttpServlet{

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final UserDAOImp userDAO = new UserDAOImp();
    private static final TokenStore tokenStore = TokenStore.getInstance();
    //private static final List<String> allowedChannels = List.of("email"); 
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Logger logger = Logger.getLogger(PasswordRecoveryReqServlet.class.getName());
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException{
        
        RecoveryRequestDTO recoveryDTO = parseRequest(req, resp);
        if(recoveryDTO == null) {
            return;
        }

        User u = userDAO.getByEmail(recoveryDTO.recipient());    
        Token t = OTPUtils.generateOTP(u);
        tokenStore.getOtpTokens().saveToken(t);

        int status = sendRecoveryEmail(t, u.getEmail());

        if(status == 200) {
            if(logger.isLoggable(Level.INFO))
                logger.log(Level.INFO, format("Sent recovery message to user: %s", u.getEmail()));
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        if(logger.isLoggable(Level.SEVERE))
            logger.log(Level.SEVERE, format("Failed to send recovery message, status code: %d", status));
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    

    private RecoveryRequestDTO parseRequest(HttpServletRequest req, HttpServletResponse resp) {
        RecoveryRequestDTO recoveryDTO;
        try{
            recoveryDTO = objectMapper.readValue(req.getInputStream(), RecoveryRequestDTO.class);
        }catch(IOException e){
            logger.log(Level.WARNING, format("Failed to parse recovery request: %s", e.getMessage()));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        /*  
        if(!allowedChannels.contains(recoveryDTO.channel())) {
             logger.log(Level.WARNING, format("Unsupported recovery channel used from IP: %s", req.getRemoteAddr()));
             resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
             resp.getWriter().write("Channel not supported");
             return null;
        }*/

        return recoveryDTO;
    }

    /* 
    private User findUserByContact(String channel, String contact) {
        User u;
        u = switch (channel) {
            case "email" -> userDAO.getByEmail(contact);
            default -> null;
        };
        return u;
    }
    */

    private int sendRecoveryEmail(Token t,String email){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(format("%s/send/%s", System.getenv("MESSAGE_SERVICE_URL"), "email")).toURI())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    objectMapper.writeValueAsString(

                        new EmailReqDTO( email,
                            "Password recovery request",
                            getEmailBody(t.getTokenValue())
                    ))))
                    .timeout(Duration.ofSeconds(200))
                    .build();

                    

            HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE,"InterruptedException while sending recovery message: %s", e);
            return 500;
        } catch (IOException | URISyntaxException e) {
            logger.log(Level.SEVERE,"Exception while sending recovery message: %s", e);
            return 500;
        }
    }

    public String getEmailBody(String t){
        return format("""
            <html>
                    <body>
                        <p>This is your password recovery code</p>
                        <h1>%s</h1>
                        <p>Insert this code at: <a href="http://188.40.183.188:4200/recovery/auth">this page</a></p>
                    </body>
                </html>
        """, t);
    }
    
}
