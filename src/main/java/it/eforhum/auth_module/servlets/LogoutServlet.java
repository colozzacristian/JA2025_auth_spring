package it.eforhum.auth_module.servlets;

import it.eforhum.auth_module.utils.JWTUtils;
import it.eforhum.auth_module.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "LogoutServlet" , urlPatterns = "/token/logout")
public class LogoutServlet extends HttpServlet{
    
    private static final TokenStore tokenStore = TokenStore.getInstance();
    
    @Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response)
	throws ServletException{

        response.setContentType("application/json");

        String authHeader = request.getHeader("Authorization");

        String jwtToken = authHeader.substring(7);

        if(tokenStore.getJwtTokens().isTokenValid(jwtToken)){

            String email = JWTUtils.getEmailFromToken(jwtToken);
            tokenStore.getJwtTokens().invalidateToken(email);

        }

        response.setStatus(200);
        
    }
}
