package it.eforhum.authModule.servlets;

import java.io.IOException;
import static java.lang.String.format;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.authModule.daos.UserDAOImp;
import it.eforhum.authModule.dtos.PasswordChangeReqDTO;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.JWTUtils;
import it.eforhum.authModule.utils.RateLimitingUtils;
import it.eforhum.authModule.utils.TokenStore;
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
        PasswordChangeReqDTO passwordChangeDTO;
        try{
            passwordChangeDTO = objectMapper.readValue(req.getInputStream(), PasswordChangeReqDTO.class
            );
        }catch(IOException e){
            logger.log(Level.WARNING, format("Failed to parse password change request: %s", e.getMessage()));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if(!tokenStore.getRecoveryToken().isTokenValid(passwordChangeDTO.token())){
            logger.log(Level.WARNING, format("Invalid or expired recovery token used from IP: %s", req.getRemoteAddr()));
            RateLimitingUtils.recordFailedAttempt(req.getRemoteAddr());
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        tokenStore.getRecoveryToken().invalidateToken(passwordChangeDTO.token());
        String email = JWTUtils.getEmailFromToken(passwordChangeDTO.token());
        User u = userDAO.getByEmail(email);
        if(u == null){
            logger.log(Level.SEVERE, format("MESSED UP BIG TIME. User not found for email extracted from token during password change: %s", email));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        userDAO.changePassword(u, passwordChangeDTO.newPassword());
        resp.setStatus(HttpServletResponse.SC_OK);
        logger.log(Level.INFO, format("Password changed successfully for user: %s", u.getEmail()));

        

    }
}
