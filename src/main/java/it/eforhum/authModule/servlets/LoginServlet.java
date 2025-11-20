package it.eforhum.authModule.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.authModule.daos.UserDAOImp;
import it.eforhum.authModule.dtos.JWTResponseDTO;
import it.eforhum.authModule.dtos.LoginDTO;
import it.eforhum.authModule.entities.Token;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.JWTUtils;
import it.eforhum.authModule.utils.PasswordHash;

@WebServlet(name="LoginServlet", urlPatterns = "/token/auth")
public class LoginServlet extends HttpServlet{
	
    UserDAOImp userDAO = new UserDAOImp();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		//application json
        response.setContentType("application/json");
        
		ObjectMapper mapper = new ObjectMapper();
        
        String body = new String(request.getInputStream().readAllBytes());
        LoginDTO loginDTO = mapper.readValue(body, LoginDTO.class);

        String email = loginDTO.email();
        
        String password = PasswordHash.crypt(loginDTO.password());

        User u = userDAO.login(email, password);

        
        if(u != null){

            response.setStatus(200);
            Token t = JWTUtils.generateJWT(u);
            response.getWriter().write(mapper.writeValueAsString(new JWTResponseDTO(t.getToken())));
            

        }else{
            response.setStatus(401); // unathorized
            System.out.println("dati: " + u.getUserId() + " " + u.getEmail());
        }
        
    }
}