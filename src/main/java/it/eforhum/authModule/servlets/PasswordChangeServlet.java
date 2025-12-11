package it.eforhum.authModule.servlets;

import java.io.IOException;

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
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
        PasswordChangeReqDTO passwordChangeDTO;
        try{
            passwordChangeDTO = objectMapper.readValue(req.getInputStream(), PasswordChangeReqDTO.class
            );
        }catch(Exception e){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        TokenStore tokenStore = TokenStore.getInstance();
        if(!tokenStore.getRecoveryToken().isTokenValid(passwordChangeDTO.token())){
            RateLimitingUtils.recordFailedAttempt(req.getRemoteAddr());
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        tokenStore.getRecoveryToken().invalidateToken(passwordChangeDTO.token());
        String email = JWTUtils.getEmailFromToken(passwordChangeDTO.token());
        User u = userDAO.getByEmail(email);
        if(u == null){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        userDAO.changePassword(u, passwordChangeDTO.newPassword());
        resp.setStatus(HttpServletResponse.SC_OK);

        

    }
}
