package it.eforhum.authModule.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.eforhum.authModule.daos.UserDAOImp;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.PasswordHash;
import it.eforhum.authModule.utils.JWTUtils;

@WebServlet(name="LoginServlet", urlPatterns = "/token/auth")
public class LoginServlet extends HttpServlet{
	
    UserDAOImp userDAO = new UserDAOImp();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		
        String email = request.getParameter("Email");
        String password = PasswordHash.crypt(request.getParameter("Password"));

        int id = userDAO.login(email, password);

        if(id != 0){
            response.setStatus(200);
            User u = userDAO.getById(id);
            request.setParameter("JWT_Token", JWTUtils.generateJWT(u));
        }else{
            response.setStatus(401);
        }
        
    }
}