package it.eforhum.auth_module.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.auth_module.daos.UserDAOImp;
import it.eforhum.auth_module.dtos.ActivationDataDTO;
import it.eforhum.auth_module.dtos.TempTokenRespDTO;
import it.eforhum.auth_module.entities.Token;
import it.eforhum.auth_module.entities.User;
import it.eforhum.auth_module.utils.JWTUtils;
import it.eforhum.auth_module.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name="ActivationAuthServlet", urlPatterns="/activation/auth")
public class ActivationAuthServlet extends HttpServlet{

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final UserDAOImp userDAO = new UserDAOImp();
    private static final TokenStore tknStore = TokenStore.getInstance();

    private static final Logger logger = Logger.getLogger(ActivationAuthServlet.class.getName());
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException{
    
        User u;
        try {
            u = check(request, response);
        } catch (IOException e) {

            logger.log(Level.WARNING, "Error reading request body", e);

            response.setStatus(400);
            return;
        }

        if(u == null){
            return;
        }
        
        if(userDAO.activateUser(u)){
            Token jwtToken = JWTUtils.generateJWT(u);
            tknStore.getJwtTokens().saveToken(jwtToken);
            response.setStatus(200);
            try {
                response.getWriter().write(mapper.writeValueAsString(new TempTokenRespDTO(jwtToken.getTokenValue())));  
            } catch (IOException e) {
                logger.log(Level.SEVERE, "IOException while writing JWT to response", e);
                response.setStatus(500);
            }
        }else{
            response.setStatus(500);
        }
    }

    private User check(HttpServletRequest request , HttpServletResponse response) throws IOException{

        User u;

        try{

            String body = new String(request.getInputStream().readAllBytes());
            ActivationDataDTO activationDataDTO = mapper.readValue(body, ActivationDataDTO.class);
            
            u = userDAO.getByEmail(activationDataDTO.email());

            if(!tknStore.getOtpTokens().isTokenValid(activationDataDTO.email(), activationDataDTO.OTP())){
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
