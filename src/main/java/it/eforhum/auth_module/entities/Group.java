package it.eforhum.auth_module.entities;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;


@Entity
//ricorda che SQL E' case sentitive anche per i nomi delle table
@Table(name ="`groups`")
public class Group {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "id")
    private Long GroupID;

    @Column(name = "GroupName", length = 50, nullable = false, unique = true)
    private String Name;


    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "groups")
    private Set<User> users;

    public Group(){}

    public Group(Long id, String name){
        this.GroupID = id;
        this.Name = name;
    }


    public Long getGroupID() {
        return GroupID;
    }

    public void setGroupID(Long GroupID) {
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