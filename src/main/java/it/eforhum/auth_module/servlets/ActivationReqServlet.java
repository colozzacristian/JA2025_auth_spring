package it.eforhum.auth_module.servlets;

import java.io.IOException;
import static java.lang.String.format;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.auth_module.daos.TokenDAO;
import it.eforhum.auth_module.daos.UserDAOImp;
import it.eforhum.auth_module.dtos.RecoveryRequestDTO;
import it.eforhum.auth_module.entities.Token;
import it.eforhum.auth_module.entities.User;
import it.eforhum.auth_module.utils.OTPUtils;
import it.eforhum.auth_module.utils.SendUtils;
import it.eforhum.auth_module.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name="ActivationReqServlet", urlPatterns="/activation")
public class ActivationReqServlet extends HttpServlet{

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final UserDAOImp userDAO = new UserDAOImp();
    private static final Logger logger = Logger.getLogger(ActivationReqServlet.class.getName());
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws  ServletException{
        RecoveryRequestDTO activationData;
        try {
           activationData = mapper.readValue(request.getInputStream().readAllBytes(), RecoveryRequestDTO.class);
            
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading request body", e);
            response.setStatus(400);
            return;
        }
        
        User u = userDAO.getByEmail(activationData.recipient());
        
        if(u == null){
            response.setStatus(400);
            if(logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, format("Activation request for non-existing user: %s", activationData.recipient()));
            return;
        }

        Token otp = OTPUtils.generateOTP(u);
        TokenStore tkStore = TokenStore.getInstance();
        TokenDAO tDAO = tkStore.getOtpTokens();
        tDAO.saveToken(otp);

        int status = SendUtils.sendActivationCode(otp, activationData.recipient());

        if(status == 200){
            response.setStatus(200);
            return;
        }

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        
    }

    
}
