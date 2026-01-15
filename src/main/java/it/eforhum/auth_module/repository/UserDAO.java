package it.eforhum.auth_module.repository;

import it.eforhum.auth_module.entity.User;


public interface UserDAO {

    public abstract User getByEmail(String email);

    public abstract User create(String email, String password, String name, String surname);

    public abstract boolean changePassword(User u, String newPassword);

    public abstract User login(String email, String password);

    public abstract boolean activateUser(User u);
}