package com.tallerwebi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class HibernateConfig {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && databaseUrl.startsWith("postgres://")) {
            // Render usa PostgreSQL
            databaseUrl = databaseUrl.replace("postgres://", "jdbc:postgresql://");
            dataSource.setUrl(databaseUrl);
            dataSource.setDriverClassName("org.postgresql.Driver");
        } else {
            // Desarrollo local con HSQLDB
            dataSource.setUrl("jdbc:hsqldb:mem:db_");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        }

        return dataSource;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan("com.tallerwebi.dominio");
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean
    public HibernateTransactionManager transactionManager() {
        return new HibernateTransactionManager(sessionFactory(dataSource()).getObject());
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();

        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && databaseUrl.contains("postgres")) {
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        } else {
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        }

        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.connection.characterEncoding", "utf8");
        properties.setProperty("hibernate.connection.CharSet", "utf8");
        properties.setProperty("hibernate.connection.useUnicode", "true");
        return properties;
    }
}
