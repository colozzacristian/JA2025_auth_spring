package it.eforhum.authModule.entities;

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
//ricorda che SQL E' case sentitive anche per i nomi delle table
@Table(name ="groups")
public class Group {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "GroupID")
    private int GroupID;

    @Column(name = "Name", length = 50, nullable = false, unique = true)
    private String Name;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "users",
        joinColumns = @JoinColumn(name = "GroupID"),
        inverseJoinColumns = @JoinColumn(name = "UserID")
    )
    private Set<User> users;

    public Group(){}

    public Group(int id, String name){
        this.GroupID = id;
        this.Name = name;
    }


    public int getGroupID() {
        return GroupID;
    }

    public void setGroupID(int GroupID) {
        this.GroupID = GroupID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }


    

}
