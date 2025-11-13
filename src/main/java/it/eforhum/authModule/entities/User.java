package it.eforhum.authModule.entities;

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
    @Column(name = "UserID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int UserId;

    @Column(name = "Email" , nullable = false, unique=true)
    private String Email;

    @Column(name = "PasswordHash" ,nullable = false)
    private String PasswordHash;

    @Column(name="FirstName")
    private String FirstName;

    @Column(name = "LastName")
    private String LastName;

    @Column(name = "Active")
    private boolean Active;

    @Column(name = "CreationDate", nullable=false)
    private LocalDateTime CreationDate;

    @Column(name = "LastAccessDate")
    private LocalDateTime LastAccessDate;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "users")
    @JoinTable(
        name = "groups",
        joinColumns = @JoinColumn(name = "UserID"),
        inverseJoinColumns = @JoinColumn(name = "GroupID")
    )
    private Set<Group> groups;


    public User(){}

    public User(int id,String email,String passwordHash, String FirstName,String LastName,boolean active,LocalDateTime creationDate,LocalDateTime lastAccessDate){
        this.UserId = id;
        this.Email = email;
        this.PasswordHash = passwordHash;
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.Active = active;
        this.CreationDate = creationDate;
        this.LastAccessDate = lastAccessDate;
    }


    public int getUserId() {
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

    public Set<Group> getGroups() {
        return groups;
    }

    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }

    public String[] getGroupsForJWT() {
        return groups.stream().map(Group::getName).toArray(String[]::new);
    }

}
