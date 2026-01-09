import java.io.IOException;
import static java.lang.String.format;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
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


    private final GroupsCheckServlet servlet = new GroupsCheckServlet();
    private static final TokenStore tokenStore = TokenStore.getInstance();
    private static Token jwtToken;
    private static String[] expectedGroups;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final UserDAOImp userDao = new UserDAOImp();
    private static final String BACKOFFICE_URL = System.getenv("BACKOFFICE_SERVICE_URL");
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeClass
    public static void setUp() {
        userDao.create("a@a.a", "pass", "first", "last");
        addUserToGroup();
        User user = userDao.getByEmail("a@a.a");
        userDao.activateUser(user);
        user.setActive(true);
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

     @AfterClass
    public static void cleanUp() {
       deleteUserByEmail("a@a.a");
    }

    private static void deleteUserByEmail(String email) {
        HttpRequest.Builder request = createBaseRequest();
        HttpResponse<String> response;

        request.DELETE();

        response = sendRequest(format("%s/api/user/delete/email/%s", BACKOFFICE_URL, email), request);

        if (response == null || response.statusCode() != 200 || userDao.getByEmail(email) != null) {
            System.err.println("Failed to delete user during cleanup: " + email);
        }
    }


    private static void addUserToGroup() {
        HttpRequest.Builder request = createBaseRequest();
        HttpResponse<String> response;
        User u = userDao.getByEmail("a@a.a");
        String requestBody = String.format("{\"userID\":\"%s\",\"groupID\":\"1\"}", u.getUserId());

        request.POST(
                HttpRequest.BodyPublishers.ofString(requestBody)
        );

        response = sendRequest(format("%s/api/user/addUserToGroup", BACKOFFICE_URL), request);

        if (response == null || response.statusCode() != 200) {
            System.err.println("Failed to add user to group during setup: a@a.a");
        }
    }

    private static Builder createBaseRequest(){
        return HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(200));
    }

    private static HttpResponse<String> sendRequest(String uri,Builder request){
        HttpResponse<String> response;

        try{
            request.uri(new URL(uri).toURI());
            response = httpClient.send(request.build(),HttpResponse.BodyHandlers.ofString());
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
            System.err.println("HTTP request interrupted");
            return null;
        }catch (URISyntaxException e){
            System.err.println( format("Invalid URI syntax: %s", uri) );
            return null;
        } catch (IOException e){
            System.err.println( format("Error sending HTTP request: %s", e.getMessage()) );
            return null;
        }
        return response;
    }

}
