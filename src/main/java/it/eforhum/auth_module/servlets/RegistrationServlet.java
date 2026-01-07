package it.eforhum.auth_module.servlets;

import java.io.IOException;
import static java.lang.String.format;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.auth_module.daos.UserDAOImp;
import it.eforhum.auth_module.dtos.RegistrationReqDTO;
import it.eforhum.auth_module.entities.Token;
import it.eforhum.auth_module.entities.User;
import it.eforhum.auth_module.utils.OTPUtils;
import it.eforhum.auth_module.utils.PasswordHash;
import it.eforhum.auth_module.utils.RateLimitingUtils;
import it.eforhum.auth_module.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name="RegistrationServlet", urlPatterns="/token/register")
public class RegistrationServlet extends HttpServlet{

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TokenStore tokenStore = TokenStore.getInstance();
    private static final UserDAOImp userDao = new UserDAOImp();
    private static final Logger logger = Logger.getLogger(RegistrationServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException{
        
        response.setContentType("text/html;charset=UTF-8");
        RegistrationReqDTO registrationDTO;
        try {
            registrationDTO = objectMapper.readValue(request.getInputStream(), RegistrationReqDTO.class); 
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading request body", e);
            response.setStatus(400);
            return;
        }

        String email = registrationDTO.email();
        String password = PasswordHash.crypt(registrationDTO.password());
        String name = registrationDTO.firstName();
        String surname = registrationDTO.lastName();

        User u = userDao.getByEmail(email);

        if(!canActivate(u, email, registrationDTO.password())){
            response.setStatus(400);
            RateLimitingUtils.recordFailedAttempt(request.getRemoteAddr());
            return;
        }
        
        if( u == null){

            u = userDao.create(email, password, name, surname);

            if(u == null){
                if(logger.isLoggable(Level.WARNING))
                    logger.log(Level.WARNING, format("Failed to create user during registration for email: %s", email));
                response.setStatus(400);
                return;
            }
        }
        
        Token t = OTPUtils.generateOTP(u);
        

        int status = 0;
        if(t == null){
            if(logger.isLoggable(Level.SEVERE))
                logger.log(Level.SEVERE, format("Failed to generate OTP token after registration for email: %s", email));
            response.setStatus(400);
            return;
        }

        status = sendOtpToken(t, u);


        if(status == 200){
            tokenStore.getOtpTokens().saveToken(t);
            response.setStatus(200);
            return;
        }            
    
        if(logger.isLoggable(Level.SEVERE))
            logger.log(Level.SEVERE, format("Failed to send OTP token after registration for email: %s", email));

        response.setStatus(400);
        

    }

    private boolean canActivate(User u, String email,String password){

        if(u != null && u.isActive()) {
            if(logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, format("Registration attempt with existing email: %s", email));
            return false;
        }

        if(u!=null && !u.isActive() && userDao.login(email, password) == null){
            if(logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, format("Registration attempt with existing but inactive email and wrong password: %s", email));
            return false;
        }

        return true;
        
    }

    private int sendOtpToken(Token t, User u){

        ActivationReqServlet servlet = new ActivationReqServlet();
        return servlet.sendActivationCode(t , u.getEmail() );
    }
}
