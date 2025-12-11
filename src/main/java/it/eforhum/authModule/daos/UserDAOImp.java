package it.eforhum.authModule.daos;

import static java.lang.String.format;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;
import it.eforhum.authModule.dtos.LoginReqDTO;
import it.eforhum.authModule.dtos.RegistrationReqDTO;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.PasswordHash;


public class UserDAOImp implements UserDAO {

    private static final Dotenv dotenv = Dotenv.load();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
        private ObjectMapper objectMapper = new ObjectMapper();



    @Override
    public User getByEmail(String email){

        User u = null;
        HttpRequest request;
        HttpResponse<String> response;

        try {
            request = HttpRequest.newBuilder()
                .uri(new URL(format("%s/api/user/getExtended/Email/%s", dotenv.get("BACKOFFICE_SERVICE_URL"), email)).toURI())
                .header("Content-Type", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(200))
                .build();
                
            response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        } catch (MalformedURLException e) {
            //Log URL error
            e.printStackTrace();
            return null;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (response.statusCode() != 200) {
            return null;
        }
        
        try {
            u = objectMapper.readValue(response.body(), User.class);
        } catch (Exception e) {
            //log deserialization error
            e.printStackTrace();
        }
       
        return u;
    }
    
    @Override
    public User login(String email,String password){

        User u = null;
        HttpRequest request;
        HttpResponse<String> response;

        try{
            request = HttpRequest.newBuilder()
                .uri(new URL(format("%s/api/user/login", dotenv.get("BACKOFFICE_SERVICE_URL"), email)).toURI())
                .header("Content-Type", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                    objectMapper.writeValueAsString(
                        new LoginReqDTO(email, PasswordHash.crypt(password))
                        ))
                )
                .timeout(Duration.ofSeconds(200))
                .build();
                
            response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        } catch (MalformedURLException e) {
            //Log URL error
            e.printStackTrace();
            return null;
            
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

        if (response.statusCode() != 200) {
            return null;
        }
        
        try {
            u = objectMapper.readValue(response.body(), User.class);
        } catch (Exception e) {
            //log deserialization error
            e.printStackTrace();
        }
       
        return u;

    }

    @Override
    public User create(String email, String password, String name, String surname){
        User u = null;
        HttpRequest request;
        HttpResponse<String> response;

        try{
            request = HttpRequest.newBuilder()
                .uri(new URL(format("%s/api/user/create", dotenv.get("BACKOFFICE_SERVICE_URL"))).toURI())
                .header("Content-Type", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                    objectMapper.writeValueAsString(
                        new RegistrationReqDTO(email, PasswordHash.crypt(password), name, surname)
                        ))

                )
                .timeout(Duration.ofSeconds(200))
                .build();
                
            response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        } catch (MalformedURLException e) {
            //Log URL error
            e.printStackTrace();
            return null;
            
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

        if (response.statusCode() != 200) {
            return null;
        }
        
        try {
            u = objectMapper.readValue(response.body(), User.class);
        } catch (Exception e) {
            //log deserialization error
            e.printStackTrace();
        }
       
        return u;

    }

    @Override
    public boolean changePassword(User u, String newPassword){
        HttpRequest request;
        HttpResponse<String> response;
        u.setPasswordHash(PasswordHash.crypt(newPassword));
        try{
            request = HttpRequest.newBuilder()
                .uri(new URL(format("%s/api/user/update", dotenv.get("BACKOFFICE_SERVICE_URL"))).toURI())
                .header("Content-Type", "application/json")
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                    objectMapper.writeValueAsString(
                        u
                        ))
                )
                .timeout(Duration.ofSeconds(200))
                .build();
                
            response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        } catch (MalformedURLException e) {
            //Log URL error
            e.printStackTrace();
            return false;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }

        if (response.statusCode() != 200) {
            //log error
            return false;
        }

        return true;
    }
    
}
