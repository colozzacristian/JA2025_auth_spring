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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import it.eforhum.auth_module.entity.User;
import it.eforhum.auth_module.repository.UserDAOImp;



public class UserDaoTest {

    @Autowired
    private  UserDAOImp userDao;

    @Value("${url.backoffice}")
    private String backofficeUrl;

    private static final HttpClient httpClient = HttpClient.newHttpClient();


    @BeforeClass
    public void setUp() {
        User u = userDao.create("a@a.a", "password123", "First", "Last");
        addUserToGroup();
        assertNotNull("User creation failed in setup", u);
    }

    @Test
    public void testGetByEmail_existingUser_shouldReturnUser() {
        User u = userDao.getByEmail("a@a.a");
        assertNotNull("User should not be null", u);
        assertEquals("Email should match", "a@a.a", u.getEmail());
    }

    @Test
    public void testGetByEmail_nonExistingUser_shouldReturnNull() {
        User u = userDao.getByEmail("nonexistent@a.a");
        assertEquals("User should be null for non-existing email", null, u);
    }

    @Test
    public void testLogin_correctCredentials_shouldReturnUser() {
        User u = userDao.login("a@a.a", "password123");
        assertNotNull("Login should succeed with correct credentials", u);
        assertEquals("Email should match", "a@a.a", u.getEmail());
    }

    @Test
    public void testLogin_incorrectCredentials_shouldReturnNull() {
        User u = userDao.login("a@a.a", "wrongpassword");
        assertEquals("Login should fail with incorrect credentials", null, u);
    }

    @Test
    public void testCreate_newUserSameEmail_shouldReturnNull() {
        User u = userDao.create("a@a.a", "newpassword", "New", "User");
        assertEquals("Creating a user with an existing email should return null", null, u);
    }

    @Test
    public void testActivateUser() {
        User u = userDao.getByEmail("a@a.a");
        assertNotNull("User should exist before activation", u);
        boolean activated = userDao.activateUser(u);
        assertEquals("User activation should return true", true, activated);
        u = userDao.getByEmail("a@a.a");
        assertEquals("User should be activated", true, u.isActive());
    }

    @Test
    public void testActivateUser_nonExistingUser_shouldReturnFalse() {
        User u = new User(99999l,"nonexistent@a.a", "First", "Last", "hashedpassword", false);
        boolean activated = userDao.activateUser(u);
        assertEquals("Activating a non-existing user should return false", false, activated);
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

        if (response == null || response.statusCode() != 200 || userDao.getByEmail(email) != null) {
            System.err.println("Failed to delete user during cleanup: " + email);
        }
    }


    private void addUserToGroup() {
        HttpRequest.Builder request = createBaseRequest();
        HttpResponse<String> response;
        User u = userDao.getByEmail("a@a.a");
        String requestBody = String.format("{\"userID\":\"%s\",\"groupID\":\"1\"}", u.getUserId());

        request.POST(
                HttpRequest.BodyPublishers.ofString(requestBody)
        );

        response = sendRequest(format("%s/api/user/addUserToGroup", backofficeUrl), request);

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