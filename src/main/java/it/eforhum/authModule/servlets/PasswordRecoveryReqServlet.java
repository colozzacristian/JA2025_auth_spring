package it.eforhum.authModule.servlets;

import static java.lang.String.format;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;

import io.github.cdimascio.dotenv.Dotenv;
import it.eforhum.authModule.daos.UserDAOImp;
import it.eforhum.authModule.dtos.RecoveryMsgReqDTO;
import it.eforhum.authModule.dtos.RecoveryReqDTO;
import it.eforhum.authModule.entities.Token;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.OTPUtils;
import it.eforhum.authModule.utils.TokenStore;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;


@WebServlet("/recovery")
public class PasswordRecoveryReqServlet extends HttpServlet{

    private ObjectMapper objectMapper = new ObjectMapper();
    private UserDAOImp userDAO = new UserDAOImp();
    private TokenStore tokenStore = TokenStore.getInstance();
    private static final List<String> allowedChannels = List.of("email"); 
    private static final Dotenv dotenv = Dotenv.load();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        
        /* return if rate limited */
        RecoveryReqDTO recoveryDTO = parseRequest(req, resp);
        if(recoveryDTO == null) {
            return;
        }

        User u = findUserByContact(recoveryDTO.channel(), recoveryDTO.contact());
        
        if(u == null) {
            /*Implement rate limiting and log ip */
            return;
        }
        
        Token t = OTPUtils.generateOTP(u);
        tokenStore.getOtpToken().saveToken(t);
        
        int status = sendRecoveryEmail(recoveryDTO,t, u.getEmail());

        if(status == 200) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.getWriter().write("Error sending recovery message");
  
    }

    private User findUserByContact(String channel, String contact) {
        User u;
        switch(channel) {
            case "email":
                u = userDAO.getByEmail(contact);
                break;
            default:
                u = null;
        }
        return u;
    }

    private RecoveryReqDTO parseRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RecoveryReqDTO recoveryDTO;
        try{
            recoveryDTO = objectMapper.readValue(req.getInputStream(), RecoveryReqDTO.class);
        }catch(IOException e){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        if(!allowedChannels.contains(recoveryDTO.channel())) {
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
            /* Log failure from mail module */

        } catch (Exception e) {

            /*Log the exception */
            return 500;
        }
    }
    
}
