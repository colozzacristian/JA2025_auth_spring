package it.eforhum.authModule.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.eforhum.authModule.daos.TokenDAO;
import it.eforhum.authModule.utils.JWTUtils;

@WebServlet(name="TokenValidation", urlPatterns = "/token/validate")
public class TokenValidationServlet extends HttpServlet{

    TokenDAO tokenDAO = new TokenDAO();
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException{

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null && !authHeader.startsWith("Bearer ")){
            response.setStatus(401);
            return;
        }

        String jwtToken = authHeader.substring(7);
        String email = JWTUtils.getEmailFromToken(jwtToken);

        

    }


}
