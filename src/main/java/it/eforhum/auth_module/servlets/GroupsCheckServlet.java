package it.eforhum.auth_module.servlets;

import java.io.IOException;
import static java.lang.String.format;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.auth_module.daos.UserDAO;
import it.eforhum.auth_module.daos.UserDAOImp;
import it.eforhum.auth_module.dtos.GroupsListRespDTO;
import it.eforhum.auth_module.dtos.InGroupsRespDTO;
import it.eforhum.auth_module.entities.User;
import it.eforhum.auth_module.utils.JWTUtils;
import it.eforhum.auth_module.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name="GroupCheck", urlPatterns = "/token/groups")
public class GroupsCheckServlet extends HttpServlet{
    
    private static final UserDAO userDAO = new UserDAOImp();
    private static final TokenStore tokenStore = TokenStore.getInstance();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = Logger.getLogger(GroupsCheckServlet.class.getName());

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException{

        String[] groupsToCheck;
        List<String> groups;
        boolean isInGroups = true;
        User user = checkTokenAndInputs(request, response);
        if(user == null) {
            return;
        }
       
        groups = List.of(user.getGroupsForJWT());
        if(request.getParameter("g") == null) {
            response.setStatus(200);
            response.setContentType("application/json");
            try {
                objectMapper.writeValue(response.getWriter(), new GroupsListRespDTO(groups.toArray(new String[0]))); 
            } catch (IOException e) {
                logger.log(Level.SEVERE, "IOException while writing groups to response", e);
                response.setStatus(500);
            }
            return;
        }

        groupsToCheck = request.getParameter("g").split(",");

        for(String g : groupsToCheck) {
            if(g.isBlank() || !groups.contains(g)) {
                isInGroups = false;
                break;
            }
            
        }
        response.setStatus(200);
        response.setContentType("application/json");
        try {
            objectMapper.writeValue(response.getWriter(), new InGroupsRespDTO(isInGroups));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException while writing groups check result to response", e);
            response.setStatus(500);
        }
    }


    private User checkTokenAndInputs(HttpServletRequest request, HttpServletResponse response) {
        String jwtToken;
        String userEmail;
        User user;
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ") ) {
            if(logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, format("Missing or invalid Authorization header from IP: %s", request.getRemoteAddr()));
            response.setStatus(401);
            return null;
        }
        jwtToken = authHeader.substring(7);
        if (!JWTUtils.isTokenSignatureValid(jwtToken)) {
            if(logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, format("Invalid token signature from IP: %s", request.getRemoteAddr()));
            response.setStatus(401);
            return null;
        }

        userEmail = JWTUtils.getEmailFromToken(jwtToken);

        if (!tokenStore.getJwtTokens().isTokenValid(userEmail,jwtToken) ) {
            if(logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, format("Invalid or expired JWT token used from IP: %s", request.getRemoteAddr()));
            response.setStatus(401);
            return null;
        }
        if((user = userDAO.getByEmail(userEmail)) == null) {
            if(logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, format("No user found for email: %s from IP: %s", userEmail, request.getRemoteAddr()));
            response.setStatus(401);
        }
        return user; 
    }
}