package it.eforhum.authModule.servlets;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.authModule.dtos.EmailRespDTO;
import it.eforhum.authModule.utils.JWTUtils;
import it.eforhum.authModule.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "GetEmailFromToken", urlPatterns = "/token/email")
public class GetEmailFromToken extends HttpServlet{

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenStore tokenStore = TokenStore.getInstance();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException{
        
        String email;

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            response.setStatus(401);
            return;
        }

        String jwtToken = authHeader.substring(7);

        if(tokenStore.getJwtToken().isTokenValid(jwtToken)){

            email = JWTUtils.getEmailFromToken(jwtToken);
            response.setContentType("application/json");

            response.setStatus(200);
            response.getWriter().write(objectMapper.writeValueAsString(new EmailRespDTO(email)));
        }else{
            response.setStatus(400);
        }

    }

}
