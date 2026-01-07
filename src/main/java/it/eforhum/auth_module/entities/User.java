package it.eforhum.auth_module.entities;


import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class User{
    
    @JsonAlias("userID")
    private Long userId;

    private String email;

    @JsonAlias("password")
    private String passwordHash;

    private String firstName;

    private String lastName;
    private boolean active;

    private Set<String> groups;


    public User(){}

    public User(String email, String passwordHash, String firstName, String lastName, boolean active){
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
     
    }

    public User(Long id,String email, String passwordHash, String firstName, String lastName, boolean active){
        this.userId = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;

    }  


    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public String[] getGroupsForJWT() {
        if (groups == null) {
            return new String[0];
        }
        return groups.toArray(new String[0]);
    }

}