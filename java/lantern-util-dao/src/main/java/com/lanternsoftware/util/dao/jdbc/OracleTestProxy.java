package com.lanternsoftware.util.dao.jdbc;

public abstract class OracleTestProxy {
    public static JdbcProxy getProxy(String _connectionString, String _username, String _password) {
        return JdbcProxy.getProxy(DatabaseType.ORACLE_11G, _connectionString, _username, _password);
    }
}
