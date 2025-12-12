package it.eforhum.authModule.servlets;

import java.io.IOException;
import static java.lang.String.format;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.authModule.daos.UserDAOImp;
import it.eforhum.authModule.dtos.JWTRespDTO;
import it.eforhum.authModule.dtos.LoginReqDTO;
import it.eforhum.authModule.entities.Token;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.JWTUtils;
import it.eforhum.authModule.utils.PasswordHash;
import it.eforhum.authModule.utils.RateLimitingUtils;
import it.eforhum.authModule.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name="LoginServlet", urlPatterns = "/token/auth")
public class LoginServlet extends HttpServlet{
	
    private final static UserDAOImp userDAO = new UserDAOImp();

    private final static Logger logger = Logger.getLogger(LoginServlet.class.getName());

    private final static TokenStore tokenStore = TokenStore.getInstance();
    

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		
        response.setContentType("application/json");
        
		ObjectMapper mapper = new ObjectMapper();
        
        String body = new String(request.getInputStream().readAllBytes());
        LoginReqDTO loginDTO = mapper.readValue(body, LoginReqDTO.class);

        String email = loginDTO.email();
        
        String password = PasswordHash.crypt(loginDTO.password());

        User u = userDAO.login(email, password);

        if(u != null){

            response.setStatus(200);
            
            Token t = JWTUtils.generateJWT(u);
            tokenStore.getJwtToken().saveToken(t);

            response.getWriter().write(mapper.writeValueAsString(new JWTRespDTO(t.getToken())));
            
        }else{
            logger.log(Level.WARNING, format("Failed login attempt for email: %s from IP: %s", email, request.getRemoteAddr()));
            RateLimitingUtils.recordFailedAttempt(request.getRemoteAddr());
            response.setStatus(401);
            return;
        }

        response.setStatus(200);
            
        Token t = JWTUtils.generateJWT(u);
        if (tokenStore.getJwtToken().isTokenValid(email)) {
            logger.log(Level.INFO, format("Invalidating previous token for email: %s from IP: %s", email, request.getRemoteAddr()));
            tokenStore.getJwtToken().invalidateToken(email);
        }

        tokenStore.getJwtToken().saveToken(t);
        logger.log(Level.INFO, format("generated token for email: %s from IP: %s", email, request.getRemoteAddr()));
        response.getWriter().write(mapper.writeValueAsString(new JWTRespDTO(t.getToken())));
        
    }
}