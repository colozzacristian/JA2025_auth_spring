package it.eforhum.auth_module.servlets;

import static java.lang.String.format;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.auth_module.daos.UserDAOImp;
import it.eforhum.auth_module.dtos.ActivationDataDTO;
import it.eforhum.auth_module.dtos.JWTRespDTO;
import it.eforhum.auth_module.entities.Token;
import it.eforhum.auth_module.entities.User;
import it.eforhum.auth_module.utils.JWTUtils;
import it.eforhum.auth_module.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name="ActivationAuthServlet", urlPatterns="/activation/auth")
public class ActivationAuthServlet extends HttpServlet{

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final UserDAOImp userDAO = new UserDAOImp();
    private static final TokenStore tknStore = TokenStore.getInstance();

    private static final Logger logger = Logger.getLogger(ActivationAuthServlet.class.getName());
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException{
    
        User u;
        String email;

        u = check(request);

        if(u == null){
            response.setStatus(400);
            return;
        }

        email = u.getEmail();

        if(!userDAO.activateUser(u)){
            if(logger.isLoggable(Level.SEVERE))
                logger.log(Level.SEVERE, format("Failed to activate user: %s", email));
            response.setStatus(500);
            return;
        }

        u = userDAO.getByEmail(u.getEmail());

        if(u == null){
            if(logger.isLoggable(Level.SEVERE))
                logger.log(Level.SEVERE, format("Failed to retrieve activated user: %s", email));
            response.setStatus(500);
            return;
        }

        Token jwtToken = JWTUtils.generateJWT(u);
        
        try {
            response.getWriter().write(mapper.writeValueAsString(new JWTRespDTO(jwtToken.getTokenValue())));  
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException while writing JWT to response", e);
            response.setStatus(500);
        }

        //prevents the JWT from being saved if it wasn't sent to the user
        tknStore.getJwtTokens().saveToken(jwtToken);
        response.setStatus(200);

    }

    private User check(HttpServletRequest request){

        User u;
        ActivationDataDTO activationDataDTO;

        try{
            activationDataDTO = mapper.readValue(request.getInputStream(), ActivationDataDTO.class);
        }catch(IOException e){
            logger.log(Level.WARNING, "Error reading request body", e);
            return null;
        }

        u = userDAO.getByEmail(activationDataDTO.email());

        if(u == null){
            if(logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, format("Activation attempt for non-existing user: %s", activationDataDTO.email()));
            return null;
        }

            
        if(!tknStore.getOtpTokens().isTokenValid(activationDataDTO.email(), activationDataDTO.otp())){
                if(logger.isLoggable(Level.WARNING))
                    logger.log(Level.WARNING, format("Invalid OTP during activation for user: %s", activationDataDTO.email()));
                return null;
        }

        return u;
    }
}
