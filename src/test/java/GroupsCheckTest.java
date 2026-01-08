import static java.lang.String.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.auth_module.daos.UserDAOImp;
import it.eforhum.auth_module.dtos.GroupsListRespDTO;
import it.eforhum.auth_module.dtos.InGroupsRespDTO;
import it.eforhum.auth_module.entities.Token;
import it.eforhum.auth_module.entities.User;
import it.eforhum.auth_module.servlets.GroupsCheckServlet;
import it.eforhum.auth_module.utils.JWTUtils;
import it.eforhum.auth_module.utils.TokenStore;




public class GroupsCheckTest {

    private final UserDAOImp userDAO = new UserDAOImp();
    private final GroupsCheckServlet servlet = new GroupsCheckServlet();
    private final TokenStore tokenStore = TokenStore.getInstance();
    private Token jwtToken;
    private String[] expectedGroups;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        User user = userDAO.getByEmail("a@a.a");
        jwtToken = JWTUtils.generateJWT(user);
        tokenStore.getJwtTokens().saveToken(jwtToken);
        expectedGroups = user.getGroupsForJWT();
    }

    @Test
    public void testGroupsNoParam() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        request.setMethod("GET");
        request.addHeader("Authorization", format("Bearer %s", jwtToken.getTokenValue()));
        try {
            servlet.doGet(request, response); 
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }

        assertEquals(200, response.getStatus());

        try {
            assertEquals(response.getContentAsString(), objectMapper.writeValueAsString(new GroupsListRespDTO(expectedGroups)));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }

    }



    @Test
    public void testGroupsParam() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer " + jwtToken.getTokenValue());
        request.addParameter("g", "USER");
        try {
            servlet.doGet(request, response); 
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }

        assertEquals(200, response.getStatus());
        try {
            assertTrue(response.getContentAsString().contains("\"isInGroups\":true"));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
        
    }

        @Test
    public void testGroupsWrongParam() {
       MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        request.setMethod("GET");
        request.addHeader("Authorization", format("Bearer %s", jwtToken.getTokenValue()));
        request.addParameter("g", "USER,,ADMIN");
        try {
            servlet.doGet(request, response); 
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }

        assertEquals(200, response.getStatus());
        try {
            assertEquals(response.getContentAsString(), objectMapper.writeValueAsString(new InGroupsRespDTO(false)));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    public void testGroupsWrongToken() {
       MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        request.setMethod("GET");
        request.addHeader("Authorization", format("Bearer a%s", jwtToken.getTokenValue()));
        request.addParameter("g", "USER,");
        try {
            servlet.doGet(request, response); 
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }

        assertEquals(401, response.getStatus());
        try {
            assertEquals("", response.getContentAsString());
            
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
        
    }
}