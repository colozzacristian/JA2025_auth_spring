package it.eforhum.authModule.utils;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateUtil {
    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

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
                e.printStackTrace();
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
        } catch (Exception ignored) {}

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
