package it.eforhum.authModule.servlets;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.authModule.daos.UserDAO;
import it.eforhum.authModule.daos.UserDAOImp;
import it.eforhum.authModule.dtos.GroupsListRespDTO;
import it.eforhum.authModule.dtos.InGroupsRespDTO;
import it.eforhum.authModule.utils.JWTUtils;
import it.eforhum.authModule.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name="GroupCheck", urlPatterns = "/token/groups")
public class GroupsCheckServlet extends HttpServlet{
    
    private UserDAO userDAO = new UserDAOImp();
    private TokenStore tokenStore = TokenStore.getInstance();
    private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {

        String[] groupsToCheck = null;
        List<String> groups;
        boolean isInGroups = true;
        String jwtToken,userEmail;
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ") ) {
            response.setStatus(401);
            return;
        }
        jwtToken = authHeader.substring(7);
        if (!JWTUtils.isTokenSignatureValid(jwtToken)) {
            response.setStatus(401);
            return;
        }

        userEmail = JWTUtils.getEmailFromToken(jwtToken);

        if (!tokenStore.getJwtToken().isTokenValid(userEmail,jwtToken) ) {
            response.setStatus(401);
            return;
        }

        groups = List.of(userDAO.getByEmail(userEmail).getGroupsForJWT());
        if(request.getParameter("g") == null) {
            response.setStatus(200);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), new GroupsListRespDTO(groups.toArray(new String[0])));
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
        objectMapper.writeValue(response.getWriter(), new InGroupsRespDTO(isInGroups));
    }
}