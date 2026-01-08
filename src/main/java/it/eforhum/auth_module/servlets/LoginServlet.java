package it.eforhum.auth_module.servlets;

import java.io.IOException;
import static java.lang.String.format;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.auth_module.daos.UserDAOImp;
import it.eforhum.auth_module.dtos.JWTRespDTO;
import it.eforhum.auth_module.dtos.LoginReqDTO;
import it.eforhum.auth_module.entities.Token;
import it.eforhum.auth_module.entities.User;
import it.eforhum.auth_module.utils.JWTUtils;
import it.eforhum.auth_module.utils.RateLimitingUtils;
import it.eforhum.auth_module.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name="LoginServlet", urlPatterns = "/token/auth")
public class LoginServlet extends HttpServlet{
	
    private static final UserDAOImp userDAO = new UserDAOImp();

    private static final Logger logger = Logger.getLogger(LoginServlet.class.getName());
    

    private static final TokenStore tokenStore = TokenStore.getInstance();
    

	@Override // TO refactor
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException {
        
		ObjectMapper mapper = new ObjectMapper();
        
        LoginReqDTO loginDTO;

        try{
            loginDTO= mapper.readValue(request.getInputStream().readAllBytes(), LoginReqDTO.class);
        }catch(IOException e){
            logger.log(Level.WARNING, format("Failed to parse login request: %s", e.getMessage()));
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String email = loginDTO.email();

        User u = userDAO.login(email, loginDTO.password());

        if(u == null){
             if(logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, format("Failed login attempt for email: %s from IP: %s", email, request.getRemoteAddr()));
            RateLimitingUtils.recordFailedAttempt(request.getRemoteAddr());
            response.setStatus(401);
            return;
        }
        
        if(!u.isActive()){
            if(logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, format("Login attempt for inactive user: %s from IP: %s", email, request.getRemoteAddr()));
            response.setStatus(403);
            return;
        }
        
        Token t = JWTUtils.generateJWT(u);
        if (tokenStore.getJwtTokens().isTokenValid(email)) {
            if(logger.isLoggable(Level.INFO))
                logger.log(Level.INFO, format("Invalidating previous token for email: %s from IP: %s", email, request.getRemoteAddr()));
            tokenStore.getJwtTokens().invalidateToken(email);
        }

        try {
            response.getWriter().write(mapper.writeValueAsString(new JWTRespDTO(t.getTokenValue())));
        } catch (IOException e) {
            if(logger.isLoggable(Level.SEVERE))
                logger.log(Level.SEVERE, "IOException while writing JWT to response", e);
            response.setStatus(500);
            return;
        }

        tokenStore.getJwtTokens().saveToken(t);
        if(logger.isLoggable(Level.INFO))
            logger.log(Level.INFO, format("generated token for email: %s from IP: %s", email, request.getRemoteAddr()));
        response.setStatus(200);
        
    }
}