package it.eforhum.authModule.servlets;

import java.io.IOException;
import static java.lang.String.format;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.authModule.daos.UserDAOImp;
import it.eforhum.authModule.dtos.RecoveryAuthReqDTO;
import it.eforhum.authModule.dtos.TempTokenRespDTO;
import it.eforhum.authModule.entities.Token;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.JWTUtils;
import it.eforhum.authModule.utils.RateLimitingUtils;
import it.eforhum.authModule.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/recovery/auth")
public class PasswordRecoveryAuthServlet extends HttpServlet{ 

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TokenStore tokenStore = TokenStore.getInstance();
    private static final UserDAOImp userDAO = new UserDAOImp();
    private static final Logger logger = Logger.getLogger(PasswordRecoveryAuthServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,ServletException {
        
        User u = performChecks(req, resp);
        if(u == null){
            return;
        }

        Token t = JWTUtils.generateJWT(u);
        tokenStore.getRecoveryToken().saveToken(t);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(objectMapper.writeValueAsString(new TempTokenRespDTO(t.getToken())));
        logger.log(Level.INFO, format("Issued temporary JWT for password recovery to user: %s", u.getEmail()));
    }
    
    private User performChecks(HttpServletRequest req, HttpServletResponse resp){
        RecoveryAuthReqDTO recoveryAuthDTO;
        try{
            recoveryAuthDTO = objectMapper.readValue(req.getInputStream(), RecoveryAuthReqDTO.class);
        }catch(IOException e){
            logger.log(Level.WARNING, format("Failed to parse recovery auth request: %s", e.getMessage()));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        if(!tokenStore.getOtpToken().isTokenValid(recoveryAuthDTO.email(), recoveryAuthDTO.otp())){
            logger.log(Level.WARNING, format("Invalid or expired OTP used from IP: %s", req.getRemoteAddr()));
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            RateLimitingUtils.recordFailedAttempt(req.getRemoteAddr());
            return null;
        }

        User u = userDAO.getByEmail(recoveryAuthDTO.email());
        if(u == null){
            logger.log(Level.SEVERE, format("MESSED UP BIG TIME. User not found for email during recovery auth: %s", recoveryAuthDTO.email()));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        
        return u;
    }
}
