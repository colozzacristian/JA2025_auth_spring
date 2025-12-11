package it.eforhum.authModule.servlets;

import java.io.IOException;

import it.eforhum.authModule.utils.JWTUtils;
import it.eforhum.authModule.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@WebServlet(name="TokenValidation", urlPatterns = "/token/validate")
public class TokenValidationServlet extends HttpServlet{

    private static final TokenStore tokenStore = TokenStore.getInstance();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException{

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            
            response.setStatus(401);
            return;
        }

        String jwtToken = authHeader.substring(7);

        if(JWTUtils.isTokenSignatureValid(jwtToken) || tokenStore.getJwtToken().isTokenValid(jwtToken)){
            response.setStatus(200);
            return;
        }
        
        response.setStatus(401);
        

    }


}
