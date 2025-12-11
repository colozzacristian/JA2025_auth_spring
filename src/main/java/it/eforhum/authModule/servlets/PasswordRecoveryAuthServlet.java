package it.eforhum.authModule.servlets;

import java.io.IOException;

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
    }
    
    private User performChecks(HttpServletRequest req, HttpServletResponse resp){
        RecoveryAuthReqDTO recoveryAuthDTO;
        try{
            recoveryAuthDTO = objectMapper.readValue(req.getInputStream(), RecoveryAuthReqDTO.class);
        }catch(IOException e){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        if(!tokenStore.getOtpToken().isTokenValid(recoveryAuthDTO.email(), recoveryAuthDTO.otp())){
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            RateLimitingUtils.recordFailedAttempt(req.getRemoteAddr());
            return null;
        }
        User u = userDAO.getByEmail(recoveryAuthDTO.email());
        if(u == null){
            //this should not happen, as the otp token was valid
            //log edge case AS IMPORTANT
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        
        return u;
    }
}
