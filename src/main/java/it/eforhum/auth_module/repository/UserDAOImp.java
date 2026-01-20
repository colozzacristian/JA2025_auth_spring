package it.eforhum.auth_module.repository;

import java.io.IOException;
import static java.lang.String.format;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.time.Duration;


import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.eforhum.auth_module.dto.ActivationReqDTO;
import it.eforhum.auth_module.dto.LoginReqDTO;
import it.eforhum.auth_module.dto.RegistrationReqDTO;
import it.eforhum.auth_module.dto.UpdateReqDTO;
import it.eforhum.auth_module.entity.User;
import it.eforhum.auth_module.service.PasswordHash;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class UserDAOImp implements UserDAO {



    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
 
    private final String backofficeUrl;

    public UserDAOImp(@Value("${url.backoffice}") String backofficeServiceUrl) {
        this.backofficeUrl = backofficeServiceUrl;
         objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    }
    @Override
    public User getByEmail(String email){

        User u = null;
        Builder request;
        HttpResponse<String> response;

        request = createBaseRequest().GET();

        response = sendRequest(format("%s/api/user/get/email/%s", backofficeUrl, email), request);

        if (response == null || response.statusCode() != 200) {
            log.warn("Failed to get user by email: {}, status code: {}", email, response != null ? response.statusCode() : "no response");
            return null;
        }
        
        try {
            u = objectMapper.readValue(response.body(), User.class);
        } catch (IOException e) {
            log.warn(format("Deserialization error when trying to get user by email: %s", email), e);
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
            log.error(format("Failed to serialize login request for user: %s", email), e);
            return null;
        }

        response = sendRequest(format("%s/api/user/login", backofficeUrl), request);

        if (response == null || response.statusCode() != 200) {
            log.warn("Failed to login: {}, status code: {}", email, response != null ? response.statusCode() : "no response");
            return null;
        }
        
        try {
            u = objectMapper.readValue(response.body(), User.class);
        } catch (IOException e) {
            log.error(format("Deserialization error when trying to login user: %s", email), e);
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
            log.error("Error serializing user object for creation", e);
            return null;
        }

        response = sendRequest(format("%s/api/user/create", backofficeUrl), request);

        if (response == null || response.statusCode() != 201) {
            log.error("Failed to create user: {}, status code: {}", email, response != null ? response.statusCode() : "no response");
            return null;
        }
        
        try {
            u = objectMapper.readValue(response.body(), User.class);
        } catch (IOException e) {
            log.error("Deserialization error when trying to create user", e);
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
            log.error("Error serializing user object for password change", e);
        }
         
        response = sendRequest(format("%s/api/user/update", backofficeUrl), request);

        if(response == null){
                log.error("No response received when changing password for user: {}", u.getEmail());
            return false;
        }

        if(response.statusCode() != 200){
                log.error("Failed to change password for user: {}, status code: {}", u.getEmail(), response.statusCode());
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
            log.error("Error serializing user object for password change", e);
        }
         
        response = sendRequest(format("%s/api/user/setActive", backofficeUrl), request);

        if(response == null){
            log.error("No response received when activating user: {}", u.getEmail());
            return false;
        }

        if(response.statusCode() != 200){
            log.error("Failed to activate user: {}, status code: {}", u.getEmail(), response.statusCode())  ;
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
            log.error("HTTP request interrupted", e);
            return null;
        }catch (URISyntaxException e){
            log.error("Invalid URI syntax: {}", uri, e);
            return null;
        } catch (IOException e){
            log.error("Error sending HTTP request", e);
            return null;
        }
        return response;
    }
    
}
