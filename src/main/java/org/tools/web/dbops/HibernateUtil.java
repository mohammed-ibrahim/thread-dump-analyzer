package org.tools.web.dbops;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Service;
import org.tools.web.model.ThreadDetail;

@Service
public class HibernateUtil {
  private static StandardServiceRegistry registry;
  private static SessionFactory sessionFactory;

  private static Object lock = new Object();

  public SessionFactory getSessionFactory() {

    if (sessionFactory == null) {
      synchronized (lock) {
        if (sessionFactory == null) {
          try {
            sessionFactory = buildSessionFactory();
          } catch (Exception e) {
            e.printStackTrace();
            if (registry != null) {
              StandardServiceRegistryBuilder.destroy(registry);
            }
          }
        }
      }
    }


    return sessionFactory;
  }

  private SessionFactory buildSessionFactory() {
    Configuration configuration = new Configuration();
    configuration.addAnnotatedClass(ThreadDetail.class);

    String jdbcUrl = getJdbcUrl();
    System.out.println("JDBC URL: " + jdbcUrl);
    configuration.setProperty("connection.driver_class", "org.h2.Driver");
    configuration.setProperty("hibernate.connection.url", jdbcUrl);
    configuration.setProperty("hibernate.connection.username", "sa");
    configuration.setProperty("hibernate.connection.password", "");
    configuration.setProperty("dialect", "org.hibernate.dialect.H2Dialect");
    configuration.setProperty("hibernate.hbm2ddl.auto", "update");
    configuration.setProperty("show_sql", "true");
    configuration.setProperty(" hibernate.connection.pool_size", "10");
    StandardServiceRegistryBuilder standardServiceRegistryBuilder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
    configuration.buildSessionFactory(standardServiceRegistryBuilder.build());
    return configuration.buildSessionFactory(standardServiceRegistryBuilder.build());
  }

  private String getJdbcUrl() {
    String dbProperty = System.getProperty("hibernate_jdbc_url");

    if (StringUtils.isBlank(dbProperty)) {
      return "jdbc:h2:file:~/tdump";
    }

    return dbProperty;
  }

  public void shutdown() {
    if (registry != null) {
      StandardServiceRegistryBuilder.destroy(registry);
    }
  }

}
