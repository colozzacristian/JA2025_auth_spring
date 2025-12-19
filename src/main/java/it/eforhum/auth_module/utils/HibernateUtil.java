package it.eforhum.auth_module.utils;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
public class HibernateUtil {
    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    private static final Logger logger = Logger.getLogger(HibernateUtil.class.getName());

    private HibernateUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                // Creare registro
                registry = new StandardServiceRegistryBuilder().configure().build();

                // Creare MetadataSources
                MetadataSources sources = new MetadataSources(registry);

                // Creare Metadata
                Metadata metadata = sources.getMetadataBuilder().build();

                // Creare SessionFactory
                sessionFactory = metadata.getSessionFactoryBuilder().build();

            } catch (Exception e) {
                logger.log(java.util.logging.Level.SEVERE, "SessionFactory creation failed", e);
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        try {
            if (sessionFactory != null) {
                sessionFactory.close();
            }
        } catch (Exception ignored) {
            
        }

        // deregistra i driver JDBC per evitare memory leak
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                // ignore
            }
        }

        // chiude il cleanup thread del driver MySQL (evita il warning)
        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }
}

