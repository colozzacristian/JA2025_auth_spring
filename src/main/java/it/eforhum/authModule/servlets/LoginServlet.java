package it.eforhum.authModule.servlets;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.authModule.daos.UserDAOImp;
import it.eforhum.authModule.dtos.JWTRespDTO;
import it.eforhum.authModule.dtos.LoginReqDTO;
import it.eforhum.authModule.entities.Token;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.JWTUtils;
import it.eforhum.authModule.utils.PasswordHash;
import it.eforhum.authModule.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name="LoginServlet", urlPatterns = "/token/auth")
public class LoginServlet extends HttpServlet{
	
    UserDAOImp userDAO = new UserDAOImp();

    TokenStore tokenStore = TokenStore.getInstance();
    

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
            response.setStatus(401);
        }
        
    }
}