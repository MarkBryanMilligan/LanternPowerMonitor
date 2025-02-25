package com.lanternsoftware.util.dao.jdbc;

import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

@DBSerializable
public class JdbcConfig {
    private DatabaseType type;
    private String username;
    private String password;
    private String hostname;
    private String database;
    private String port;

    public JdbcConfig() {
    }

    public JdbcConfig(DatabaseType _type, String _username, String _password, String _hostname, String _database, String _port) {
        type = _type;
        username = _username;
        password = _password;
        hostname = _hostname;
        database = _database;
        port = _port;
    }

    public DatabaseType getType() {
        return type;
    }

    public void setType(DatabaseType _type) {
        type = _type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String _username) {
        username = _username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String _password) {
        password = _password;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String _hostname) {
        hostname = _hostname;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String _database) {
        database = _database;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String _port) {
        port = _port;
    }

    public String getConnectionString() {
        StringBuilder conn = new StringBuilder("jdbc:");
        if (type == DatabaseType.MYSQL)
            conn.append("mysql");
        else
            conn.append("oracle:thin");
        conn.append("://");
        conn.append(hostname);
        conn.append(":");
        conn.append(port);
        if (NullUtils.isNotEmpty(database)) {
            conn.append("/");
            conn.append(database);
        }
        return conn.toString();
    }
}
