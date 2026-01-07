package it.eforhum.auth_module.dtos;
import it.eforhum.auth_module.entities.User;

import java.time.LocalDate;

public class UpdateReqDTO {
    private long userId;
    private String email;
    private String password;
    private boolean active;
    private String firstName;
    private String lastName;
    private LocalDate creationDate = null;
    private LocalDate lastAccessDate = null;

    
    public UpdateReqDTO(User u){
        this.userId = u.getUserId();
        this.email = u.getEmail();
        this.password = u.getPasswordHash();
        this.active = u.isActive();
        this.firstName = u.getFirstName();
        this.lastName = u.getLastName();
        this.creationDate = null;
        this.lastAccessDate = null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public long getUserId() {
        return userId;
    }



    public void setUserId(long userId) {
        this.userId = userId;
    }



    public String getPassword() {
        return password;
    }



    public void setPassword(String password) {
        this.password = password;
    }



    public boolean isActive() {
        return active;
    }



    public void setActive(boolean active) {
        this.active = active;
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

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDate getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(LocalDate lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    
    
    
}
