package com.lanternsoftware.util.dao.jdbc.preparedparameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Implementation of {@link PreparedParameter} for SQL-Nulls
 */
public class PreparedNull implements PreparedParameter {
    private final int sqlType;
    private final String typeName;

    /**
     * Default Constructor
     * 
     * @param _sqlType
     *            - Integer representing the SQL type code defined in {@link java.sql.Types}
     */
    public PreparedNull(int _sqlType) {
        sqlType = _sqlType;
        typeName = null;
    }

    /**
     * Type Name Constructor
     * 
     * @param _sqlType
     *            - Integer representing the SQL type code defined in {@link java.sql.Types}
     * @param _typeName
     *            - String representing the fully-qualified name of an SQL user-defined type; ignored if the parameter is
     *            not a user-defined
     */
    public PreparedNull(int _sqlType, String _typeName) {
        sqlType = _sqlType;
        typeName = _typeName;
    }

    /**
     * {@inheritDoc}
     */
    public int addToStatement(int _startIdx, PreparedStatement _statement) throws SQLException {
        if (_statement == null)
            return _startIdx;

        if (typeName == null)
            _statement.setNull(_startIdx, sqlType);
        else
            _statement.setNull(_startIdx, sqlType, typeName);

        return ++_startIdx;
    }

}
