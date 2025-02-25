package com.lanternsoftware.util.dao.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcProxy extends AbstractJdbcProxy {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcProxy.class);
    private Connection connection;
    
    public JdbcProxy(Connection _connection) {
        connection = _connection;
    }
    
    @Override
    public Connection getConnection() {
        return connection;
    }
    
    @Override
    public boolean isConnected() {
        try {
            return connection.isValid(10);
        }
        catch (Exception _e) {
            LOG.error("Failed to get a jdbc connection", _e);
            return false;
        }
    }
    
    @Override
    public boolean alwaysClose() {
        return false;
    }

    public static JdbcProxy getProxy(JdbcConfig _config) {
        return getProxy(_config.getType(), _config.getConnectionString(), _config.getUsername(), _config.getPassword());
    }

    public static JdbcProxy getProxy(DatabaseType _type, String _connectionString, String _username, String _password) {
        String driver;
        if (_type == DatabaseType.MYSQL)
            driver = "com.mysql.cj.jdbc.Driver";
        else if (_type == DatabaseType.CACHE)
            driver = "com.intersys.jdbc.CacheDriver";
        else
            driver = "oracle.jdbc.driver.OracleDriver";
        try {
            DriverManager.registerDriver(Class.forName(driver).asSubclass(Driver.class).getDeclaredConstructor().newInstance());
            JdbcProxy proxy = new JdbcProxy(DriverManager.getConnection(_connectionString, _username, _password));
            proxy.databaseType = _type;
            return proxy;
        }
        catch (Exception _e) {
            LOG.error("Failed to load JDBC driver for database type: " + _type, _e);
            return null;
        }
    }
}
