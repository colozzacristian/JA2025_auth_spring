

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eforhum.authModule.daos.UserDAOImp;
import it.eforhum.authModule.dtos.GroupsListRespDTO;
import it.eforhum.authModule.dtos.InGroupsRespDTO;
import it.eforhum.authModule.entities.Token;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.servlets.GroupsCheckServlet;
import it.eforhum.authModule.utils.JWTUtils;
import it.eforhum.authModule.utils.TokenStore;




public class GroupsCheckTest {

    private UserDAOImp userDAO = new UserDAOImp();
    private GroupsCheckServlet servlet = new GroupsCheckServlet();
    private TokenStore tokenStore = TokenStore.getInstance();
    private Token jwtToken;
    private String[] expectedGroups;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        User User = userDAO.getByEmail("a@a.a");
        jwtToken = JWTUtils.generateJWT(User);
        tokenStore.getJwtToken().saveToken(jwtToken);
        expectedGroups =User.getGroupsForJWT();
    }

    @Test
    public void testGroupsNoParam() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer " + jwtToken.getToken());
        try {
            servlet.doGet(request, response); 
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }

        assertEquals(response.getStatus(), 200);

        try {
            assertEquals(response.getContentAsString(), objectMapper.writeValueAsString(new GroupsListRespDTO(expectedGroups)));
        } catch (Exception e) {
        }

    }



    @Test
    public void testGroupsParam() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer " + jwtToken.getToken());
        request.addParameter("g", "USER");
        try {
            servlet.doGet(request, response); 
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }

        assertEquals(response.getStatus(), 200);
        try {
            assertTrue(response.getContentAsString().contains("\"isInGroups\":true"));
        } catch (Exception e) {
        }
        
    }

        @Test
    public void testGroupsWrongParam() {
       MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer " + jwtToken.getToken());
        request.addParameter("g", "USER,,ADMIN");
        try {
            servlet.doGet(request, response); 
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }

        assertEquals(response.getStatus(), 200);
        try {
            assertEquals(response.getContentAsString(), objectMapper.writeValueAsString(new InGroupsRespDTO(false)));
        } catch (Exception e) {
        }
    }

    public void testGroupsWrongToken() {
       MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer a" + jwtToken.getToken());
        request.addParameter("g", "USER,");
        try {
            servlet.doGet(request, response); 
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }

        assertEquals(response.getStatus(), 401);
        try {
        assertEquals(response.getContentAsString(), "");
            
        } catch (Exception e) {
            System.out.println("Unexpected exception in testGroupsWrongToken:");
            System.exit(-1);
        }
        
    }
}