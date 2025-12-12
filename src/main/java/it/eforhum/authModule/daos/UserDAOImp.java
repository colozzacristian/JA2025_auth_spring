package it.eforhum.authModule.daos;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.HibernateUtil;
import it.eforhum.authModule.utils.PasswordHash;


public class UserDAOImp implements UserDAO {

    private static final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    @Override
    public User getByEmail(String email){

        User u = null;

        try(Session session = sessionFactory.openSession()){

            u = session.createQuery("FROM User u WHERE u.Email = :email", User.class)
                       .setParameter("email", email)
                       .uniqueResult();

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

            Query<User> query = session.createQuery("From User",User.class);

            allUserList = query.list();
        }

        return allUserList;
    }

    @Override
    public List<User> getInactive(){

        List<User> inactiveUsers = null;

        try(Session session = sessionFactory.openSession()){

            Query<User> query = session.createQuery("FROM User u WHERE u.Active = false",User.class);

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

            Query<User> query = session.createQuery("FROM User u WHERE u.CreationDate = " + creationDate,User.class);
            
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

            Query<User> query = session.createQuery("FROM User u WHERE u.LastAccessDate < " + date,User.class);

            usersList = query.list();           
        }catch(Exception e){
            e.printStackTrace();
        }
        return usersList;
    }
    
    @Override
    public User login(String email,String password){

        User u = null;
        try(Session session = sessionFactory.openSession()){
            
            u = session.createQuery("FROM User u WHERE u.Email = :email AND u.PasswordHash = :password", User.class)
                    .setParameter("email", email)
                    .setParameter("password",password) 
                    .uniqueResult();
            

            if(u != null){
                u.setLastAccessDate(LocalDateTime.now());
                Transaction tr = session.beginTransaction();
                tr.commit();
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }

        return u;

    }

    public User create(String email, String password, String name, String surname){
        User u = null;
        try(Session session = sessionFactory.openSession()){
           Transaction tr = session.beginTransaction();
           u = new User(email, password, name, surname, false, LocalDateTime.now(), null);
           session.persist(u);
              
            tr.commit();
            
        }catch(Exception e){
            e.printStackTrace();
        }
        return u;
    }

    public void changePassword(User u, String newPassword){
        try(Session session = sessionFactory.openSession()){
            Transaction tr = session.beginTransaction();
            u.setPasswordHash(PasswordHash.crypt(newPassword));
            session.merge(u);
            tr.commit();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
