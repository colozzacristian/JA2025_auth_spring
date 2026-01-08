package it.eforhum.auth_module.servlets;

import java.io.IOException;
import static java.lang.String.format;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.auth_module.daos.UserDAOImp;
import it.eforhum.auth_module.dtos.PasswordChangeReqDTO;
import it.eforhum.auth_module.entities.User;
import it.eforhum.auth_module.utils.JWTUtils;
import it.eforhum.auth_module.utils.RateLimitingUtils;
import it.eforhum.auth_module.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@WebServlet("/password/change-password")
public class PasswordChangeServlet extends HttpServlet{

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TokenStore tokenStore = TokenStore.getInstance();
    private static final UserDAOImp userDAO = new UserDAOImp();

    private static final Logger logger = Logger.getLogger(PasswordChangeServlet.class.getName());
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        PasswordChangeReqDTO passwordChangeDTO;
        try{
            passwordChangeDTO = objectMapper.readValue(req.getInputStream(), PasswordChangeReqDTO.class
            );
        }catch(IOException e){
            logger.log(Level.WARNING, format("Failed to parse password change request: %s", e.getMessage()));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if(!tokenStore.getRecoveryTokens().isTokenValid(passwordChangeDTO.token())){
            if(logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, format("Invalid or expired recovery token used from IP: %s", req.getRemoteAddr()));
            RateLimitingUtils.recordFailedAttempt(req.getRemoteAddr());
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        tokenStore.getRecoveryTokens().invalidateToken(passwordChangeDTO.token());
        String email = JWTUtils.getEmailFromToken(passwordChangeDTO.token());
        User u = userDAO.getByEmail(email);
        if(u == null){
            if(logger.isLoggable(Level.SEVERE))
                logger.log(Level.SEVERE, format("MESSED UP BIG TIME. User not found for email extracted from token during password change: %s", email));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }        

        if(! userDAO.changePassword(u, passwordChangeDTO.newPassword())){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            if(logger.isLoggable(Level.SEVERE))
                logger.log(Level.SEVERE, format("Failed to change password for user: %s", u.getEmail()));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        if(logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, format("Password changed successfully for user: %s", u.getEmail()));
    }
}
