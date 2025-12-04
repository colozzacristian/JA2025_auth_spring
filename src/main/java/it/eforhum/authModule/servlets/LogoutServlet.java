package it.eforhum.authModule.servlets;

import java.io.IOException;

import it.eforhum.authModule.daos.TokenDAO;
import it.eforhum.authModule.utils.JWTUtils;
import it.eforhum.authModule.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "LogoutServlet" , urlPatterns = "/token/logout")
public class LogoutServlet extends HttpServlet{
    
    TokenStore tokenStore = TokenStore.getInstance();
    TokenDAO tkDAO = tokenStore.getJwtToken();
    
    @Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {

        response.setContentType("application/json");

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null && !authHeader.startsWith("Bearer ")){
            response.setStatus(401);
            return;
        }

        String jwtToken = authHeader.substring(7);

        if(JWTUtils.isTokenSignatureValid(jwtToken)){

            String email = JWTUtils.getEmailFromToken(jwtToken);
            tkDAO.invalidateToken(email);

            response.setStatus(200);

        }else{
            response.setStatus(400);
        }
    }
}
