package com.lanternsoftware.util.dao.jdbc.preparedinstatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import com.lanternsoftware.util.dao.jdbc.AbstractJdbcProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedBoolean;
import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedByte;
import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedBytes;
import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedDouble;
import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedEnum;
import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedFloat;
import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedInBatchedParameter;
import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedInt;
import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedLong;
import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedNull;
import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedObject;
import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedParameter;
import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedShort;
import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedString;

/**
 * Class that will execute batched {@link PreparedStatement}s supporting {@link InClause}(s).
 * <p />
 * An in-clause can be defined using an in-clause delimiter: <b>{in}</b>. This delimiter will be replaced by the
 * appropriate column displays and values. Because the number of parameters within each in-clause is dynamic, the query
 * will be restructured every time it's executed to reflect the correct number of ?-delimiters for parameters. This
 * syntax supports both single and multi-column in-clauses as well as multiple in-clauses per select. The batch size for
 * the overall SQL statement may be customized. Depending on the number of columns and parameters that are passed in for
 * each in-clause, several special cases may occur:
 * <p />
 * <strong>Special Case 1:</strong> The batch-size is less than the number of {@link InClauseColumn}s In this situation,
 * the batch size will be adjusted to the number of columns. For example, if you set the batch size to 1 and you pass in
 * 2 columns, your optimized batch size will be 2. <br />
 * <strong>Special Case 2:</strong> No {@link PreparedParameter}s are set on any {@link InClauseColumn}s In this
 * situation, the behavior will depend on the ReturnAllIfEmpty flag passed in during the constructor. If true, the
 * in-clause will be replaced with a <code>'1=1'</code>, meaning all rows would be returned; false will replace with
 * <code>'1=0'</code>. <br />
 * <strong>Special Case 3:</strong> A single {@link InClauseColumn} with a single {@link PreparedParameter}s is set. In
 * this situation, the in-clause will be optimized to be an equals (i.e. <code>=?</code> and not <code>in(?)</code>).
 * <p />
 * Some thought must be given when constructing queries and using this utility. If you are using a single in-clause, the
 * parameters will be batched into optimized sizes (i.e. if you pass in a list of 76, it may be batched to a list of
 * 100). As a result, the resulting {@link ResultSet}s may not produce unique rows across multiple {@link #next()}
 * calls.
 * <p />
 * If you are using multiple in-clauses, you may end up executing several queries to accommodate all combinations of
 * parameters. This can be minimized by using appropriate batch-sizes (typically the default). Using or-statements
 * between in-clauses may produce multiple queries for the same data (the queries will now be batched). <br />
 * <strong>Example:</strong> <br />
 * Batch Size: 2 <br />
 * Original Query:
 * <code>select * from table where (record_id) in ('A','B','C') and str_val in('Y','Z') and dbl_val in(1, 2, 3)</code>
 * <br />
 * In-Clause Delimited Query: <code>select * from table where {in} and {in} and {in}</code> <br />
 * <br />
 * In this case, we will end up with 4 distinct statements to represent the data within the 3 in-clauses: <br />
 * Statement 1: <br />
 * Query: <code>select * from table where (record_id) in (?,?) and str_val = in(?,?) and dbl_val in(?,?)</code> <br />
 * Parameters: <br />
 * 1. Collection<PreparedParameter>: Parameters: 'A', 'B' <br />
 * 2. Collection<PreparedParameter>: Parameters: 'Y', 'Z' <br />
 * 3. Collection<PreparedParameter>: Parameters: 1, 2 <br />
 * <br />
 * Statement 2: <br />
 * Query: <code>select * from table where record_id = ? and str_val = in(?,?) and dbl_val in(?,?)</code> <br />
 * Parameters: <br />
 * 1. Collection<PreparedParameter>: Parameters: 'C' <br />
 * 2. Collection<PreparedParameter>: Parameters: 'Y', 'Z' <br />
 * 3. Collection<PreparedParameter>: Parameters: 1, 2 <br />
 * <br />
 * Statement 3: <br />
 * Query: <code>select * from table where (record_id) in (?,?) and str_val = in(?,?) and dbl_val = ?</code> <br />
 * Parameters: <br />
 * 1. Collection<PreparedParameter>: Parameters: 'A', 'B' <br />
 * 2. Collection<PreparedParameter>: Parameters: 'Y', 'Z' <br />
 * 3. Collection<PreparedParameter>: Parameters: 3 <br />
 * <br />
 * Statement 4: <br />
 * Query: <code>select * from table where (record_id) = ? and str_val = in(?,?) and dbl_val = ?</code> <br />
 * Parameters: <br />
 * 1. Collection<PreparedParameter>: Parameters: 'C' <br />
 * 2. Collection<PreparedParameter>: Parameters: 'Y', 'Z' <br />
 * 3. Collection<PreparedParameter>: Parameters: 3
 * <p />
 * <strong>A Note on {@link PreparedParameter}s:</strong> The current functionality of PreparedInStatement (like the
 * current SQL standards) does not support null-parameters in a "in()" query. <br />
 * i.e. <code>select * from table where (record_id) in ('1', '2', NULL)</code> would not get you rows where the
 * record_id is null. <br />
 * To do this you would need to manually format your query to something like:
 * <code>select * from table where record_id IS NULL or {in}</code>, which would then get translated by
 * PreparedInStatement to: <code>select * from table where record_id IS NULL or (record_id) in ('1', '2')</code>.
 */
public class PreparedInStatement {
    private static final Logger LOG = LoggerFactory.getLogger(PreparedInStatement.class);
    private final Map<Integer, PreparedParameter> m_mapParameters = new HashMap<Integer, PreparedParameter>();
    private final InClauseBuilder clauseBuilder = new InClauseBuilder();
    private final AbstractJdbcProxy proxy;
    private final Connection connection;
    private final String query;
    private ResultSet resultSet;

    private Collection<InClauseStatement> statements;

    /**
     * Default Constructor
     *
     * @param _query
     *            - SQL query to execute
     * @param _proxy
     *            - {@link AbstractJdbcProxy} that should be used to retrieve the {@link PreparedStatement}s.
     * @throws SQLException
     */
    public PreparedInStatement(String _query, AbstractJdbcProxy _proxy) throws SQLException {
        query = _query;
        proxy = _proxy;
        connection = proxy.getConnection();
        statements = null;

        if (proxy == null)
            throw new SQLException("invalid_connection");
    }

    /**
     * Sets the designated parameter to the given {@link Boolean} value. The driver converts this to an SQL BIT value when
     * it sends it to the database.
     *
     * @param _parameterIndex
     *            - Integer representing the index of the parameter in the SQL {@link PreparedStatement}
     * @param _val
     *            - {@link Boolean} value
     */
    public void setBoolean(int _parameterIndex, boolean _val) {
        setParameter(_parameterIndex, new PreparedBoolean(_val));
    }

    /**
     * Sets the designated parameter to the given {@link byte}. The driver converts this to an SQL TINYINT value when it
     * sends it to the database.
     *
     * @param _parameterIndex
     *            - Integer representing the index of the parameter in the SQL {@link PreparedStatement}
     * @param _byte
     *            - {@link byte} value
     */
    public void setByte(int _parameterIndex, byte _byte) {
        setParameter(_parameterIndex, new PreparedByte(_byte));
    }

    /**
     * Sets the designated parameter to the given bytes. The driver converts this to an SQL VARBINARY or
     * LONGVARBINARY (depending on the argument's size relative to the driver's limits on VARBINARY values) when it sends it
     * to the database.
     *
     * @param _parameterIndex
     *            - Integer representing the index of the parameter in the SQL {@link PreparedStatement}
     * @param _bytes
     *            - values
     */
    public void setBytes(int _parameterIndex, byte[] _bytes) {
        setParameter(_parameterIndex, new PreparedBytes(_bytes));
    }

    /**
     * Sets the designated parameter to the given Java double value. The driver converts this to an SQL DOUBLE value when it
     * sends it to the database.
     *
     * @param _parameterIndex
     *            - Integer representing the index of the parameter in the SQL {@link PreparedStatement}
     * @param _val
     *            - {@link double} value
     */
    public void setDouble(int _parameterIndex, double _val) {
        setParameter(_parameterIndex, new PreparedDouble(_val));
    }

    /**
     * Sets the designated parameter to the given Java float value. The driver converts this to an SQL FLOAT value when it
     * sends it to the database.
     *
     * @param _parameterIndex
     *            - Integer representing the index of the parameter in the SQL {@link PreparedStatement}
     * @param _val
     *            - {@link float} value
     */
    public void setFloat(int _parameterIndex, float _val) {
        setParameter(_parameterIndex, new PreparedFloat(_val));
    }

    /**
     * Sets the designated parameter to the given Java int value. The driver converts this to an SQL INTEGER value when it
     * sends it to the database.
     *
     * @param _parameterIndex
     *            - Integer representing the index of the parameter in the SQL {@link PreparedStatement}
     * @param _val
     *            - {@link int} value
     */
    public void setInt(int _parameterIndex, int _val) {
        setParameter(_parameterIndex, new PreparedInt(_val));
    }

    /**
     * Sets the designated parameter to the given Java long value. The driver converts this to an SQL BIGINT value when it
     * sends it to the database.
     *
     * @param _parameterIndex
     *            - Integer representing the index of the parameter in the SQL {@link PreparedStatement}
     * @param _val
     *            - {@link long} value
     */
    public void setLong(int _parameterIndex, long _val) {
        setParameter(_parameterIndex, new PreparedLong(_val));
    }

    /**
     * Sets the designated parameter to SQL NULL.
     *
     * @param _parameterIndex
     *            - Integer representing the index of the parameter in the SQL {@link PreparedStatement}
     * @param _sqlType
     *            - Integer representing the SQL type code defined in {@link java.sql.Types}
     */
    public void setNull(int _parameterIndex, int _sqlType) {
        setParameter(_parameterIndex, new PreparedNull(_sqlType));
    }

    /**
     * Sets the designated parameter to SQL NULL. This version of the method setNull should be used for user-defined types
     * and REF type parameters. Examples of user-defined types include: STRUCT, DISTINCT, JAVA_OBJECT, and named array
     * types. <strong>Note:</strong> To be portable, applications must give the SQL type code and the fully-qualified SQL
     * type name when specifying a NULL user-defined or REF parameter. In the case of a user-defined type the name is the
     * type name of the parameter itself. For a REF parameter, the name is the type name of the referenced type. If a JDBC
     * driver does not need the type code or type name information, it may ignore it. Although it is intended for
     * user-defined and Ref parameters, this method may be used to set a null parameter of any JDBC type. If the parameter
     * does not have a user-defined or REF type, the given typeName is ignored.
     *
     * @param _parameterIndex
     *            - Integer representing the index of the parameter in the SQL {@link PreparedStatement}
     * @param _sqlType
     *            - Integer representing the SQL type code defined in {@link java.sql.Types}
     * @param _typeName
     *            - String representing the fully-qualified name of an SQL user-defined type; ignored if the parameter is
     *            not a user-defined type or REF
     */
    public void setNull(int _parameterIndex, int _sqlType, String _typeName) {
        setParameter(_parameterIndex, new PreparedNull(_sqlType, _typeName));
    }

    /**
     * Sets the designated parameter to the given Java short value. The driver converts this to an SQL SMALLINT value when
     * it sends it to the database.
     *
     * @param _parameterIndex
     *            - Integer representing the index of the parameter in the SQL {@link PreparedStatement}
     * @param _val
     *            - {@link short} value
     */
    public void setShort(int _parameterIndex, short _val) {
        setParameter(_parameterIndex, new PreparedShort(_val));
    }

    /**
     * Sets the designated parameter to the given {@link String} value. The driver converts this to an SQL VARCHAR or
     * LONGVARCHAR value (depending on the argument's size relative to the driver's limits on VARCHAR values) when it sends
     * it to the database.
     *
     * @param _parameterIndex
     *            - Integer representing the index of the parameter in the SQL {@link PreparedStatement}
     * @param _val
     *            - {@link String} value
     */
    public void setString(int _parameterIndex, String _val) {
        setParameter(_parameterIndex, new PreparedString(_val));
    }

    public void setObject(int _parameterIndex, Object _object) {
        setParameter(_parameterIndex, new PreparedObject(_object));
    }

    public void setParameter(int _parameterIndex, PreparedParameter _parameter) {
        m_mapParameters.put(_parameterIndex, _parameter);
    }

    /**
     * Sets a Collection of {@link InClauseColumn}s, making up an in-clause. The parameters contained in the
     * {@link InClauseColumn}s will be batched appropriately before execution.
     * <p />
     * Note that if the list of parameters (contained within the {@link InClauseColumn}s are empty), the in-clause will be
     * replaced with a '1 = 0'.
     *
     * @param _parameterIndex
     *            - Integer representing the index of the parameter in the SQL {@link PreparedStatement}
     * @param _columns
     *            - a variable list of {@link InClauseColumn}s that make up the in-clause
     * @throws SQLException
     *             If at least one InClauseColumn is not given or if the size of each InClauseColumn parameter count is not
     *             the same.
     */
    public void setInClause(int _parameterIndex, InClauseColumn... _columns) throws SQLException {
        setInClause(_parameterIndex, false, _columns);
    }

    @SuppressWarnings("rawtypes")
    public void setInClause(int _parameterIndex, String _columnDisplay, Collection<Object> _values) throws SQLException {
        Queue<PreparedParameter> queueParameters = new LinkedList<PreparedParameter>();
        for (Object value : _values) {
            if (value instanceof String)
                queueParameters.add(new PreparedString((String) value, false));
            else if (value instanceof Boolean)
                queueParameters.add(new PreparedBoolean((Boolean) value));
            else if (value instanceof Byte)
                queueParameters.add(new PreparedByte((Byte) value));
            else if (value instanceof Double)
                queueParameters.add(new PreparedDouble((Double) value));
            else if (value instanceof Enum)
                queueParameters.add(new PreparedEnum((Enum) value));
            else if (value instanceof Float)
                queueParameters.add(new PreparedFloat((Float) value));
            else if (value instanceof Integer)
                queueParameters.add(new PreparedInt((Integer) value));
            else if (value instanceof Long)
                queueParameters.add(new PreparedLong((Long) value));
            else if (value instanceof Short)
                queueParameters.add(new PreparedShort((Short) value));
        }
        setInClause(_parameterIndex, false, new InClauseColumn(_columnDisplay, queueParameters));
    }

    /**
     * Sets a Collection of {@link InClauseColumn}s, making up an in-clause. The parameters contained in the
     * {@link InClauseColumn}s will be batched appropriately before execution.
     *
     * @param _parameterIndex
     *            - Integer representing the index of the parameter in the SQL {@link PreparedStatement}
     * @param _returnAllIfEmpty
     *            - a boolean flag that determines the resulting behavior if the {@link InClauseColumn}s are empty. If true,
     *            the in-clause will be replaced with a '1=1', meaning all rows would be returned; false will replace with
     *            '1=0'
     * @param _columns
     *            - a variable list of {@link InClauseColumn}s that make up the in-clause
     * @throws SQLException
     *             If at least one InClauseColumn is not given or if the size of each InClauseColumn parameter count is not
     *             the same.
     */
    public void setInClause(int _parameterIndex, boolean _returnAllIfEmpty, InClauseColumn... _columns) throws SQLException {
        if (_columns == null || (_columns.length > 0 && _columns[0] == null)) {
            throw new SQLException("invalid_in_clause_columns");
        }
        int size = 0;
        if (_columns.length > 0) {
            size = _columns[0].getParameterCnt();
            for (InClauseColumn column : _columns) {
                if (column == null)
                    continue;

                if (size != column.getParameterCnt())
                    throw new SQLException("invalid_parameter_cnt");
            }
        }
        clauseBuilder.addClause(new InClause(_parameterIndex, _returnAllIfEmpty, _columns));
    }

    /**
     * Method which defines a maximum batch size used to calculate in-clause batch sizes.
     *
     * @param _batchSize
     */
    public void setMaxBatchSize(int _batchSize) {
        clauseBuilder.setMaxBatchSize(_batchSize);
    }

    /**
     * Method which tells if there are remaining batched {@link PreparedStatement}s left to be executed.
     *
     * @return boolean; true if there are more {@link PreparedStatement}s left to be executed.
     */
    public boolean hasNext() {
        if (statements == null)
            statements = clauseBuilder.buildStatements();
        return !statements.isEmpty();
    }

    private PreparedStatement getNextPopulatedStatement() throws SQLException {
        if (statements == null)
            throw new SQLException("invalid_statements");

        String sQuery = "";
        if (!statements.isEmpty()) {
            InClauseStatement inStatement = statements.iterator().next();
            if (inStatement != null) {
                sQuery = inStatement.formatQuery(query);
                Map<Integer, InClauseBatchedParameter> mapParameters = inStatement.getParameters();
                if (mapParameters != null && !mapParameters.isEmpty()) {
                    Iterator<Entry<Integer, InClauseBatchedParameter>> iter = mapParameters.entrySet().iterator();
                    while (iter.hasNext()) {
                        Entry<Integer, InClauseBatchedParameter> entry = iter.next();
                        if (entry == null)
                            continue;

                        m_mapParameters.put(entry.getKey(), new PreparedInBatchedParameter(entry.getValue()));
                    }
                }

                statements.remove(inStatement);
            }
        }
        else
            sQuery = query;

        PreparedStatement statement = proxy.getPreparedStatement(connection, sQuery);
        int idx = 1;
        Iterator<PreparedParameter> iter = m_mapParameters.values().iterator();
        while (iter.hasNext()) {
            PreparedParameter parameter = iter.next();
            if (parameter == null)
                continue;

            idx = parameter.addToStatement(idx, statement);
        }
        return statement;
    }

    /**
     * Method that will call executeQuery() the next batched {@link PreparedStatement}. This method will also close the
     * previous {@link ResultSet} that was returned. Note that the consumer is responsible for closing the last
     * {@link ResultSet} (when next() is not called again). It's important to remember that this will execute the current
     * batched {@link PreparedStatement}; others will be executed. Therefore, you are not guaranteed that the data being
     * returned is unique across multiple calls to next(). It is required to call {@link #hasNext()} before executing this
     * method, otherwise a {@link SQLException} will be thrown.
     *
     * @return the next {@link ResultSet} to be evaluated
     * @throws SQLException
     *             If a database access error occurs or the SQL statement is invalid.
     */
    public ResultSet next() throws SQLException {
        if (resultSet != null)
            resultSet.close();
        PreparedStatement statement = null;
        try {
            statement = getNextPopulatedStatement();
            resultSet = statement.executeQuery();
            statement.clearWarnings();
            statement.clearBatch();
            statement.clearParameters();
            return resultSet;
        }
        catch (SQLException e) {
            if (connection != null)
                connection.close();
            if (statement != null)
                statement.close();
            if (resultSet != null)
                resultSet.close();
            throw e;
        }
    }

    /**
     * Method that will call executeUpdate() on all batched {@link PreparedStatement}s.
     *
     * @return an Integer representing the affected row count (across all batched {@link PreparedStatement}s).
     * @throws SQLException
     *             If a database access error occurs or the SQL statement is invalid.
     */
    public int executeUpdate() throws SQLException {
        int rowCnt = 0;
        while (hasNext()) {
            PreparedStatement statement = getNextPopulatedStatement();
            rowCnt += statement.executeUpdate();
            statement.close();
        }
        connection.close();
        return rowCnt;
    }

    /**
     * Method that will clear out any remaining {@link PreparedStatement}s as well as close the last-used {@link ResultSet}.
     * Clear should be called if the statement is being reused, in between executions.
     *
     * @throws SQLException
     */
    public void clear() throws SQLException {
        try {
            statements = null;
            clauseBuilder.reset();
            m_mapParameters.clear();
            if (resultSet != null)
                resultSet.close();
            resultSet = null;
        }
        catch (Throwable t) {
        }
    }
}
