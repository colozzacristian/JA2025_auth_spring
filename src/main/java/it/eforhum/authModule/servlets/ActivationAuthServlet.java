package it.eforhum.authModule.servlets;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.authModule.daos.UserDAOImp;
import it.eforhum.authModule.dtos.ActivationDataDTO;
import it.eforhum.authModule.dtos.TempTokenRespDTO;
import it.eforhum.authModule.entities.Token;

import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.JWTUtils;
import it.eforhum.authModule.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name="ActivationAuthServlet", urlPatterns="/activate/authenticate")
public class ActivationAuthServlet extends HttpServlet{

    private ObjectMapper mapper = new ObjectMapper();
    private UserDAOImp userDAO = new UserDAOImp();
    private TokenStore tknStore = TokenStore.getInstance();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException{
        
        User u = check(request,response);

        if(u == null){
            return;
        }
        
        if(userDAO.activateUser(u)){
            Token jwtToken = JWTUtils.generateJWT(u);
            tknStore.getJwtToken().saveToken(jwtToken);
            response.setStatus(200);
            response.getWriter().write(mapper.writeValueAsString(new TempTokenRespDTO(jwtToken.getToken())));
        }else{
            response.setStatus(500);
        }
    }

    private User check(HttpServletRequest request , HttpServletResponse response) throws IOException{

        User u = null;

        try{

            String body = new String(request.getInputStream().readAllBytes());
            ActivationDataDTO activationDataDTO = mapper.readValue(body, ActivationDataDTO.class);
            
            u = userDAO.getByEmail(activationDataDTO.email());

            if(!tknStore.getOtpToken().isTokenValid(activationDataDTO.email(), activationDataDTO.OTP())){
                response.setStatus(401);
                return null;
            }
            
        }catch(IOException e){
            response.setStatus(400);
            return null;
        }

        if(u == null){
            response.setStatus(400);
        }

        return u;
    }
}
