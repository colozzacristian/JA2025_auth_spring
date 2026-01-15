package it.eforhum.auth_module.entity;


import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;


@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
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

    public String[] getGroupsForJWT() {
        if (groups == null) {
            return new String[0];
        }
        String[] groupsArray = new String[groups.size()];
        int i = 0;
        for (String string : groups) {
            groupsArray[i++] = string;
        }
        return groupsArray;
    }

}