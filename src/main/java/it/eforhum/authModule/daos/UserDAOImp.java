package it.eforhum.authModule.daos;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.HibernateUtil;


public class UserDAOImp implements UserDAO {

    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    @Override
    public User getByEmail(String email){

        User u = null;

        try(Session session = sessionFactory.openSession()){

            u = session.get(User.class,email);

        }catch(Exception e){
            e.printStackTrace();
        }

        return u;
    }

    @Override
    public User getById(int id){

        User u = null;

        try(Session session = sessionFactory.openSession()){

            u = session.get(User.class,id);

        }catch(Exception e){
            e.printStackTrace();
        }

        return u;
    }

    @Override
    public List<User> getAll(){

        List<User> allUserList = null;

        try(Session session = sessionFactory.openSession()){

            Query<User> query = session.createQuery("From users",User.class);

            allUserList = query.list();
        }

        return allUserList;
    }

    @Override
    public List<User> getInactive(){

        List<User> inactiveUsers = null;

        try(Session session = sessionFactory.openSession()){

            Query<User> query = session.createQuery("From users WHERE users.Active = false",User.class);

            inactiveUsers = query.list();           
        }catch(Exception e){
            e.printStackTrace();
        }

        return inactiveUsers;

    }

    @Override
    public  List<User> getByCreationDate(LocalDateTime creationDate){
        
        List<User> usersList = null;

        try(Session session = sessionFactory.openSession()){

            Query<User> query = session.createQuery("From users WHERE users.CreationDate = " + Timestamp.valueOf(creationDate) ,User.class);
            
            usersList = query.list();           
        }catch(Exception e){
            e.printStackTrace();
        }
        return usersList;       
    }

    @Override
    public  List<User> getLastActiveBeforeDate(LocalDateTime date){

        List<User> usersList = null;

        try(Session session = sessionFactory.openSession()){

            Query<User> query = session.createQuery("From users WHERE users.LastAccessDate < " + Timestamp.valueOf(date),User.class);

            usersList = query.list();           
        }catch(Exception e){
            e.printStackTrace();
        }
        return usersList;
    }
    
    @Override
    public boolean login(String email,String password){

        try(Session session = sessionFactory.openSession()){
            
            Query<User> query = session.createQuery("FROM users WHERE users.PasswordHash = " + password + " AND users.Email = " + email, User.class);
            
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }

        return false;

    }
    
}
