package it.eforhum.authModule.daos;

import it.eforhum.authModule.entities.User;


public interface UserDAO {

    public abstract User getByEmail(String email);

    public abstract User login(String email, String password);

    public abstract User create(String email, String password, String name, String surname);

    public boolean changePassword(User u, String newPassword);

}
