package it.eforhum.authModule.daos;

import java.time.LocalDateTime;
import java.util.List;

import it.eforhum.authModule.entities.User;


public interface UserDAO {

    public abstract User getByEmail(String email);

    public abstract User getById(int id);

    public abstract List<User> getAll();

    public abstract List<User> getInactive();

    public abstract List<User> getByCreationDate(LocalDateTime date);

    public abstract List<User> getLastActiveBeforeDate(LocalDateTime date);

    public abstract User login(String email, String password);
}