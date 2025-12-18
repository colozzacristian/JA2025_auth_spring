package it.eforhum.authModule.servlets;

import java.io.IOException;
import static java.lang.String.format;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.authModule.daos.UserDAOImp;
import it.eforhum.authModule.dtos.RegistrationReqDTO;
import it.eforhum.authModule.entities.Token;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.OTPUtils;
import it.eforhum.authModule.utils.PasswordHash;
import it.eforhum.authModule.utils.RateLimitingUtils;
import it.eforhum.authModule.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name="RegistrationServlet", urlPatterns="/token/register")
public class RegistrationServlet extends HttpServlet{

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private TokenStore tokenStore = TokenStore.getInstance();
    private static final UserDAOImp userDao = new UserDAOImp();
    private static final Logger logger = Logger.getLogger(RegistrationServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException{
        
        response.setContentType("text/html;charset=UTF-8");
        String body = new String(request.getInputStream().readAllBytes());
        RegistrationReqDTO registrationDTO = objectMapper.readValue(body, RegistrationReqDTO.class);

        String email = registrationDTO.email();
        String password = PasswordHash.crypt(registrationDTO.password());
        String name = registrationDTO.firstName();
        String surname = registrationDTO.lastName();

        if(userDao.getByEmail(email) != null){
            logger.log(Level.WARNING, format("Registration attempt with existing email: %s", email));
            RateLimitingUtils.recordFailedAttempt(request.getRemoteAddr());
            response.setStatus(400);
            return;
        }
        
        User user = userDao.create(email, password, name, surname);


        if(user != null){
            
            Token t = OTPUtils.generateOTP(user);
            tokenStore.getOtpToken().saveToken(t);

            int status = 0;
            if(t != null){
                status = sendOtpToken(t, user);
            }

            if(status == 200){
                response.setStatus(200);
                return;
            }
            
        }else{
            response.setStatus(400);
            return;
        }
        
        logger.log(Level.SEVERE, format("Failed to create response after registration for email: %s", email));
        response.setStatus(400);
        

    }

    private int sendOtpToken(Token t, User u){

        ActivationReqServlet servlet = new ActivationReqServlet();
        int status = servlet.sendActivationCode(t , u.getEmail() );

        if(status == 200){
            return status;
        }

        return status;
    }
}
