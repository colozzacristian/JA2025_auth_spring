package it.eforhum.auth_module.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.auth_module.dtos.EmailRespDTO;
import it.eforhum.auth_module.utils.JWTUtils;
import it.eforhum.auth_module.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@WebServlet(name = "GetEmailFromToken", urlPatterns = "/token/email")
public class GetEmailFromToken extends HttpServlet{

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TokenStore tokenStore = TokenStore.getInstance();

    private static final Logger logger = Logger.getLogger(GetEmailFromToken.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException{
        
        String email;

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            response.setStatus(401);
            return;
        }

        String jwtToken = authHeader.substring(7);

        if(tokenStore.getJwtTokens().isTokenValid(jwtToken)){

            email = JWTUtils.getEmailFromToken(jwtToken);
            response.setContentType("application/json");

            response.setStatus(200);

            try {
                response.getWriter().write(objectMapper.writeValueAsString(new EmailRespDTO(email))); 
            } catch (IOException e) {
                logger.log(Level.SEVERE, "IOException while writing email to response", e);
                response.setStatus(500);
            }
        }else{
            response.setStatus(400);
        }
    }
}
