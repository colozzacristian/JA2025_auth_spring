package it.eforhum.auth_module.entities;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;


@Entity
@Table(name = "users")
public class User{
    
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "Email" , nullable = false, unique=true)
    private String email;

    @Column(name = "PasswordHash" ,nullable = false)
    private String passwordHash;

    @Column(name="FirstName")
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "Active")
    private boolean active;

    @Column(name = "CreationDate", nullable=false)
    private LocalDateTime creationDate;

    @Column(name = "LastAccessDate")
    private LocalDateTime lastAccessDate;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "UserGroups",
        joinColumns = {@JoinColumn(name = "user_id")},
        inverseJoinColumns = {@JoinColumn(name = "group_id")}
    )
    private Set<String> groups;


    public User(){}

    public User(String email, String passwordHash, String firstName, String lastName, boolean active, LocalDateTime creationDate, LocalDateTime lastAccess){
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
        this.creationDate = creationDate;
        this.lastAccessDate = lastAccess;
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

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(LocalDateTime lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
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
        return groups.stream().toArray(String[]::new);
    }

}