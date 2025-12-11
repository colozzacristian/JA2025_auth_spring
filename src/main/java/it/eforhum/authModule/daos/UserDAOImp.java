package it.eforhum.authModule.daos;

import java.io.IOException;
import static java.lang.String.format;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;
import it.eforhum.authModule.dtos.LoginReqDTO;
import it.eforhum.authModule.dtos.RegistrationReqDTO;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.PasswordHash;


public class UserDAOImp implements UserDAO {

    private static final Dotenv dotenv = Dotenv.load();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(UserDAOImp.class.getName());

    @Override
    public User getByEmail(String email){

        User u = null;
        Builder request;
        HttpResponse<String> response;

        request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(200));

        response = sendRequest(format("%s/api/user/getExtended/Email/%s", dotenv.get("BACKOFFICE_SERVICE_URL"), email), request);

        if (response.statusCode() != 200) {
            return null;
        }
        
        try {
            u = objectMapper.readValue(response.body(), User.class);
        } catch (IOException e) {
            logger.log(Level.WARNING, format("Deserialization error when trying to get user by email: %s", email), e);
        }
       
        return u;
    }
    
    @Override
    public User login(String email,String password){

        User u = null;
        Builder request;
        HttpResponse<String> response;

         request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(200));
        try{
            request.POST(
                HttpRequest.BodyPublishers.ofString(
                    objectMapper.writeValueAsString(
                        new LoginReqDTO(email, PasswordHash.crypt(password))
            )));
        }catch(IOException e){
            logger.log(Level.SEVERE, format("Failed to serialize login request for user: %s", email), e);
            return null;
        }

        response = sendRequest(format("%s/api/user/login", dotenv.get("BACKOFFICE_SERVICE_URL")), request);

        if (response.statusCode() != 200) {
            return null;
        }
        
        try {
            u = objectMapper.readValue(response.body(), User.class);
        } catch (IOException e) {
            logger.log(Level.SEVERE, format("Deserialization error when trying to login user: %s", email), e);
        }
       
        return u;

    }

    @Override
    public User create(String email, String password, String name, String surname){
        User u = null;
        Builder request;
        HttpResponse<String> response;


        request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(200));

        try{
            request.POST(
                HttpRequest.BodyPublishers.ofString(
                    objectMapper.writeValueAsString(
                        new RegistrationReqDTO(email, PasswordHash.crypt(password), name, surname)
            )));
        }catch(IOException e){
            logger.log(Level.SEVERE, "Error serializing user object for creation", e);
            return null;
        }

        response = sendRequest(format("%s/api/user/create", dotenv.get("BACKOFFICE_SERVICE_URL")), request);

        if (response.statusCode() != 200) {
            return null;
        }
        
        try {
            u = objectMapper.readValue(response.body(), User.class);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Deserialization error when trying to create user", e);
        }
       
        return u;

    }

    @Override
    public boolean changePassword(User u, String newPassword){
        Builder request;
        HttpResponse<String> response;
        u.setPasswordHash(PasswordHash.crypt(newPassword));


        request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(200));

        try {
            request.POST(
                    HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(u)
                    )
                );
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error serializing user object for password change", e);
        }
         
        response = sendRequest(format("%s/api/user/update", dotenv.get("BACKOFFICE_SERVICE_URL")), request);

        return response != null && response.statusCode() == 200;
    }


    private HttpResponse<String> sendRequest(String Uri,Builder request){
        HttpResponse<String> response;

        try{
            request.uri(new URL(Uri).toURI());
            response = httpClient.send(request.build(),HttpResponse.BodyHandlers.ofString());
        }catch (URISyntaxException e){
            logger.log(Level.SEVERE, format("Invalid URI syntax: %s", Uri), e);
            return null;
        } catch (IOException | InterruptedException e){
            logger.log(Level.SEVERE, "Error sending HTTP request", e);
            return null;
        }
        return response;
    }
    
}
