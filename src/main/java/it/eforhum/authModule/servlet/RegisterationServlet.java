package it.eforhum.authModule.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.eforhum.authModule.daos.UserDAOImp;
import it.eforhum.authModule.entities.User;

@WebServlet(name="RegisterationServlet", urlPatterns="/token/register")
public class RegisterationServlet extends HttpServlet{

    UserDAOImp userDao = new UserDAOImp();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException{
        response.setContentType("text/html;charset=UTF-8");

        String email = request.getParameter("Email");
        String password = request.getParameter("Password");
        String name = request.getParameter("FirstName");
        String surname = request.getParameter("LastName");

        int id = userDao.create(email, password, name, surname);

        if(id != 0){
            response.setStatus(200);
            User u = userDao.getById(id);
            request.setParameter("JWT_Token",jwtutils.generateJWT(u));
        }else{
            response.setStatus(400);
        }

    }

}
