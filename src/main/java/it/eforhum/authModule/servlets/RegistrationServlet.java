package it.eforhum.authModule.servlets;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.authModule.daos.UserDAOImp;
import it.eforhum.authModule.dtos.JWTResponseDTO;
import it.eforhum.authModule.dtos.RegistrationDTO;
import it.eforhum.authModule.entities.Token;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.JWTUtils;
import it.eforhum.authModule.utils.PasswordHash;
@WebServlet(name="RegistrationServlet", urlPatterns="/token/register")
public class RegistrationServlet extends HttpServlet{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    UserDAOImp userDao = new UserDAOImp();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException{
        response.setContentType("text/html;charset=UTF-8");
        String body = new String(request.getInputStream().readAllBytes());
        RegistrationDTO registrationDTO = objectMapper.readValue(body, RegistrationDTO.class);

        String email = registrationDTO.email();
        String password = PasswordHash.crypt(registrationDTO.password());
        String name = registrationDTO.firstName();
        String surname = registrationDTO.lastName();

        if(userDao.getByEmail(email) != null){
            response.setStatus(400);
            return;
        }
        
        
        User user = userDao.create(email, password, name, surname);

        if(user != null){
            response.setStatus(200);
            Token t = JWTUtils.generateJWT(user);
            response.getWriter().write(objectMapper.writeValueAsString(new JWTResponseDTO(t.getToken())));
        }else{
            response.setStatus(400);
        }

    }

}
