package com.blpsteam.blpslab1.configuration;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.postgresql.xa.PGXADataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.primary.url:jdbc:postgresql://localhost:5432/db1}")
    private String primaryUrl;

    @Value("${spring.datasource.primary.username:postgres}")
    private String primaryUsername;

    @Value("${spring.datasource.primary.password:postgres}")
    private String primaryPassword;

    @Value("${spring.datasource.secondary.url:jdbc:postgresql://localhost:5433/db2}")
    private String secondaryUrl;

    @Value("${spring.datasource.secondary.username:postgres}")
    private String secondaryUsername;

    @Value("${spring.datasource.secondary.password:postgres}")
    private String secondaryPassword;

    private AtomikosDataSourceBean createXaDataSource(
            String uniqueResourceName, 
            String schema, 
            String url, 
            String username, 
            String password) {
        
        String urlWithoutPrefix = url.replace("jdbc:postgresql://", "");
        String[] parts = urlWithoutPrefix.split("/");
        String hostAndPort = parts[0];
        String databaseName = parts.length > 1 ? parts[1] : "";
        
        String[] hostPortParts = hostAndPort.split(":");
        String host = hostPortParts[0];
        int port = hostPortParts.length > 1 ? Integer.parseInt(hostPortParts[1]) : 5432;

        PGXADataSource pgXaDataSource = new PGXADataSource();
        pgXaDataSource.setServerNames(new String[]{host});
        pgXaDataSource.setPortNumbers(new int[]{port});
        pgXaDataSource.setDatabaseName(databaseName);
        pgXaDataSource.setUser(username);
        pgXaDataSource.setPassword(password);

        Properties props = new Properties();
        props.setProperty("currentSchema", schema);

        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
        xaDataSource.setXaDataSource(pgXaDataSource);
        xaDataSource.setXaProperties(props);
        xaDataSource.setUniqueResourceName(uniqueResourceName);
        xaDataSource.setPoolSize(5);



        return xaDataSource;
    }

    @Bean(name = "primaryDataSource")
    @Primary
    public DataSource primaryDataSource() {
        return createXaDataSource("primaryDS", "public", primaryUrl, primaryUsername, primaryPassword);
    }

    @Bean(name = "secondaryDataSource")
    public DataSource secondaryDataSource() {
        return createXaDataSource("secondaryDS", "public", secondaryUrl, secondaryUsername, secondaryPassword);
    }

}
