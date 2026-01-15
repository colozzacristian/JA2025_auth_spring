package it.eforhum.auth_module;

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
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import it.eforhum.auth_module.repository.UserDAO;
import it.eforhum.auth_module.entity.Token;
import it.eforhum.auth_module.entity.User;
import it.eforhum.auth_module.service.JWTUtils;
import it.eforhum.auth_module.repository.TokenStore;




@SpringBootTest
@AutoConfigureMockMvc
public class GroupsCheckTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private MockMvc mockMvc;
    
    private final TokenStore tokenStore;
    private final String backofficeUrl;
    private final JWTUtils jwtUtils;
    private final UserDAO userDAO;



    private  String[] expectedGroups;
    private Token jwtToken;



    public GroupsCheckTest(UserDAO userDAO,@Value("${url.backoffice}") String backofficeUrl, MockMvc mockMvc, TokenStore tokenStore, JWTUtils jwtUtils) {
        this.userDAO = userDAO;
        this.backofficeUrl = backofficeUrl;    
        this.tokenStore = tokenStore;
        this.mockMvc = mockMvc;
        this.jwtUtils = jwtUtils;
    }

    @BeforeClass
    public void setUp() {
        userDAO.create("a@a.a", "pass", "first", "last");
        addUserToGroup();
        User user = userDAO.getByEmail("a@a.a");
        userDAO.activateUser(user);
        user.setActive(true);
        jwtToken = jwtUtils.generateJWT(user);
        tokenStore.getJwtTokens().saveToken(jwtToken);
        expectedGroups = user.getGroupsForJWT();
    }

    @Test
    public void testGroupsNoParam() throws Exception {
        mockMvc.perform(get("/token/groups")
                .header("Authorization", format("Bearer %s", jwtToken.getTokenValue())))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.groups").isArray());
    }



    @Test
    public void testGroupsParam() throws Exception {
        mockMvc.perform(get("/token/groups")
                .param("g", "USER")
                .header("Authorization", format("Bearer %s", jwtToken.getTokenValue())))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isInGroups").value(true));
    }

    @Test
    public void testGroupsWrongParam() throws Exception {
        mockMvc.perform(get("/token/groups")
                .param("g", "USER,,ADMIN")
                .header("Authorization", format("Bearer %s", jwtToken.getTokenValue())))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isInGroups").value(false));
    }

    @Test
    public void testGroupsWrongToken() throws Exception {
        mockMvc.perform(get("/token/groups")
                .param("g", "USER,")
                .header("Authorization", format("Bearer a%s", jwtToken.getTokenValue())))
                .andExpect(status().isUnauthorized());
    }

     @AfterClass
    public void cleanUp() {
       deleteUserByEmail("a@a.a");
    }

    private void deleteUserByEmail(String email) {
        HttpRequest.Builder request = createBaseRequest();
        HttpResponse<String> response;

        request.DELETE();

        response = sendRequest(format("%s/api/user/delete/email/%s", backofficeUrl, email), request);

        if (response == null || response.statusCode() != 200 || userDAO.getByEmail(email) != null) {
            System.err.println("Failed to delete user during cleanup: " + email);
        }
    }


    private void addUserToGroup() {
        HttpRequest.Builder request = createBaseRequest();
        HttpResponse<String> response;
        User u = userDAO.getByEmail("a@a.a");
        String requestBody = String.format("{\"userID\":\"%s\",\"groupID\":\"1\"}", u.getUserId());

        request.POST(
                HttpRequest.BodyPublishers.ofString(requestBody)
        );

        response = sendRequest(format("%s/api/user/addUserToGroup", backofficeUrl), request);

        if (response == null || response.statusCode() != 200) {
            System.err.println("Failed to add user to group during setup: a@a.a");
        }
    }

    private Builder createBaseRequest(){
        return HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(200));
    }

    private HttpResponse<String> sendRequest(String uri,Builder request){
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
