package it.eforhum.authModule.entities;

import java.time.LocalDateTime;
import java.util.Set;



public class User{
    
    private Long UserId;

    private String Email;

    private String PasswordHash;

    private String FirstName;

    private String LastName;

    private boolean Active;

    private LocalDateTime CreationDate;

    private LocalDateTime LastAccessDate;

    private Set<String> groups;


    public User(){}

    public User(String email, String passwordHash, String firstName, String lastName, boolean active, LocalDateTime creationDate, LocalDateTime lastAccess){
        this.Email = email;
        this.PasswordHash = passwordHash;
        this.FirstName = firstName;
        this.LastName = lastName;
        this.Active = active;
        this.CreationDate = creationDate;
        this.LastAccessDate = lastAccess;
    }

    public User(Long id,String email, String passwordHash, String firstName, String lastName, boolean active, LocalDateTime creationDate, LocalDateTime lastAccess){
        this.UserId = id;
        this.Email = email;
        this.PasswordHash = passwordHash;
        this.FirstName = firstName;
        this.LastName = lastName;
        this.Active = active;
        this.CreationDate = creationDate;
        this.LastAccessDate = lastAccess;
    }  


    public Long getUserId() {
        return UserId;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public String getPasswordHash() {
        return PasswordHash;
    }

    public void setPasswordHash(String PasswordHash) {
        this.PasswordHash = PasswordHash;
    }

    public boolean isActive() {
        return Active;
    }

    public void setActive(boolean Active) {
        this.Active = Active;
    }

    public LocalDateTime getCreationDate() {
        return CreationDate;
    }

    public void setCreationDate(LocalDateTime CreationDate) {
        this.CreationDate = CreationDate;
    }

    public LocalDateTime getLastAccessDate() {
        return LastAccessDate;
    }

    public void setLastAccessDate(LocalDateTime LastAccessDate) {
        this.LastAccessDate = LastAccessDate;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String FirstName) {
        this.FirstName = FirstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String LastName) {
        this.LastName = LastName;
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
