package it.eforhum.auth_module.daos;


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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.eforhum.auth_module.dtos.ActivationReqDTO;
import it.eforhum.auth_module.dtos.LoginReqDTO;
import it.eforhum.auth_module.dtos.RegistrationReqDTO;
import it.eforhum.auth_module.dtos.UpdateReqDTO;
import it.eforhum.auth_module.entities.User;
import it.eforhum.auth_module.utils.PasswordHash;


public class UserDAOImp implements UserDAO {

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(UserDAOImp.class.getName());
    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    }
    private static final String BACKOFFICE_SERVICE_URL = System.getenv("BACKOFFICE_SERVICE_URL");

    @Override
    public User getByEmail(String email){

        User u = null;
        Builder request;
        HttpResponse<String> response;

        request = createBaseRequest().GET();

        response = sendRequest(format("%s/api/user/get/email/%s", BACKOFFICE_SERVICE_URL, email), request);

        if (response == null || response.statusCode() != 200) {
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

        request = createBaseRequest();
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

        response = sendRequest(format("%s/api/user/login", BACKOFFICE_SERVICE_URL), request);

        if (response == null || response.statusCode() != 200) {
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


        request = createBaseRequest();

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

        response = sendRequest(format("%s/api/user/create", BACKOFFICE_SERVICE_URL), request);

        if (response == null || response.statusCode() != 201) {
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
        UpdateReqDTO updateReqDTO = new UpdateReqDTO(u);
        updateReqDTO.setPassword(PasswordHash.crypt(newPassword));

        request = createBaseRequest();

        try {
            request.PUT(
                    HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(updateReqDTO)
                    )
                );
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error serializing user object for password change", e);
        }
         
        response = sendRequest(format("%s/api/user/update", BACKOFFICE_SERVICE_URL), request);

        if(response == null){
            if(logger.isLoggable(Level.SEVERE))
                logger.log(Level.SEVERE, format("No response received when changing password for user: %s", u.getEmail()));
            return false;
        }

        if(response.statusCode() != 200){
            if(logger.isLoggable(Level.SEVERE))
                logger.log(Level.SEVERE, format("Failed to change password for user: %s, status code: %d", u.getEmail(), response.statusCode()));
            return false;
        }

        return true;
    }

    @Override
    public boolean activateUser(User u){

        
        Builder request;
        HttpResponse<String> response;
    
        ActivationReqDTO activationReqDTO = new ActivationReqDTO(u.getUserId(), true);

        request = createBaseRequest();

        try {
            request.PUT(
                    HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(activationReqDTO)
                    )
                );
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error serializing user object for password change", e);
        }
         
        response = sendRequest(format("%s/api/user/setActive", BACKOFFICE_SERVICE_URL), request);

        if(response == null){
            if(logger.isLoggable(Level.SEVERE))
                logger.log(Level.SEVERE, format("No response received when activating user: %s", u.getEmail()));
            return false;
        }

        if(response.statusCode() != 200){
            if(logger.isLoggable(Level.SEVERE))
                logger.log(Level.SEVERE, format("Failed to activate user: %s, status code: %d", u.getEmail(), response.statusCode()));
            return false;
        }

        return true;
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
            logger.log(Level.SEVERE, "HTTP request interrupted", e);
            return null;
        }catch (URISyntaxException e){
            logger.log(Level.SEVERE, format("Invalid URI syntax: %s", uri), e);
            return null;
        } catch (IOException e){
            logger.log(Level.SEVERE, "Error sending HTTP request", e);
            return null;
        }
        return response;
    }
    
}
