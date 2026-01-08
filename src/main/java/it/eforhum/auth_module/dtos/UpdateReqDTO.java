package it.eforhum.auth_module.dtos;
import it.eforhum.auth_module.entities.User;

public class UpdateReqDTO {
    private long userID;
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    
    public UpdateReqDTO(User u){
        this.userID = u.getUserId();
        this.email = u.getEmail();
        this.password = u.getPasswordHash();
        this.firstName = u.getFirstName();
        this.lastName = u.getLastName();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public long getUserID() {
        return userID;
    }



    public void setUserID(long userID) {
        this.userID = userID;
    }



    public String getPassword() {
        return password;
    }



    public void setPassword(String password) {
        this.password = password;
    }



    public String getFirstName() {
        return firstName;
    }



    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }



    public String getLastName() {
        return lastName;
    }



    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    
    
    
}
