package com.blpsteam.blpslab1.configuration;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.postgresql.xa.PGXADataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class DataSourceConfig {

    private AtomikosDataSourceBean createXaDataSource(String uniqueResourceName, String schema, int port, String databaseName) {
        PGXADataSource pgXaDataSource = new PGXADataSource();
        pgXaDataSource.setServerNames(new String[]{"localhost"});
        pgXaDataSource.setPortNumbers(new int[]{port});
        pgXaDataSource.setDatabaseName(databaseName);
        pgXaDataSource.setUser("postgres");
        pgXaDataSource.setPassword("postgres");

        Properties props = new Properties();
        props.setProperty("currentSchema", schema);

        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
        xaDataSource.setXaDataSource(pgXaDataSource);
        xaDataSource.setXaProperties(props);
        xaDataSource.setUniqueResourceName(uniqueResourceName);
        xaDataSource.setPoolSize(5);

//        xaDataSource.setBorrowConnectionTimeout(5); // сколько секунд ждать соединения из пула (если БД лежит — упадёт через 5 сек)
//        xaDataSource.setMaintenanceInterval(2);    // как часто проверять соединения в пуле
//        xaDataSource.setMaxIdleTime(10);            // сколько секунд держать неиспользуемое соединение


        return xaDataSource;
    }

    @Bean(name = "primaryDataSource")
    @Primary
    public DataSource primaryDataSource() {
        return createXaDataSource("primaryDS", "public", 5432, "db1");  // база db1
    }

    @Bean(name = "secondaryDataSource")
    public DataSource secondaryDataSource() {
        return createXaDataSource("secondaryDS", "public", 5433, "db2"); // база db2
    }

}
