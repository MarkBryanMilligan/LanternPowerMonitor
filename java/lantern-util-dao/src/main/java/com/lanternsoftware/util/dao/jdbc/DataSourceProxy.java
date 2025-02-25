package com.lanternsoftware.util.dao.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lanternsoftware.util.NullUtils;

public class DataSourceProxy extends AbstractJdbcProxy {
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceProxy.class);
    private DataSource dataSource;
    private String schemaName;

    public DataSourceProxy(String _jndiDataSourceName, String _schemaName) {
        this(_jndiDataSourceName);
        schemaName = _schemaName;
    }
    public DataSourceProxy(String _jndiDataSourceName) {
        try {
            dataSource = (DataSource)new InitialContext().lookup(_jndiDataSourceName);
        }
        catch (Exception e) {
            try {
                dataSource = (DataSource)new InitialContext().lookup("java:/comp/env/"+_jndiDataSourceName);
            }
            catch (Exception _e) {
                LOG.error("Error looking up " + _jndiDataSourceName, e);
            }
        }
    }

    public DataSourceProxy(DataSource _dataSource) {
        dataSource = _dataSource;
        try {
            DatabaseMetaData metaData = getConnection().getMetaData();
            if (metaData.getDatabaseProductName().equals("Oracle") && (metaData.getDatabaseMajorVersion() >= 12))
                databaseType = DatabaseType.ORACLE_12C;
        }
        catch (SQLException _e) {
            LOG.error("Could not get database type", _e);
        }
    }

    @Override
    public Connection getConnection() {
        try {
            Connection conn = dataSource.getConnection();
            if (NullUtils.isNotEmpty(schemaName) && (conn != null))
                conn.setSchema(schemaName);
            return conn;
        }
        catch (SQLException _e) {
            LOG.error("Failed to get a jdbc connection", _e);
            return null;
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return getConnection().isValid(10);
        }
        catch (Exception _e) {
            LOG.error("Failed to get a jdbc connection", _e);
            return false;
        }
    }

    @Override
    public boolean alwaysClose() {
        return true;
    }
}
