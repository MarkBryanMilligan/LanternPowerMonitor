package com.lanternsoftware.util.dao.jdbc.preparedparameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import com.lanternsoftware.util.dao.jdbc.preparedinstatement.InClauseColumn;

/**
 * Implementation of {@link PreparedParameter} for {@link Float}s
 */
public class PreparedFloat implements PreparedParameter {
    private final Float val;

    /**
     * Default Constructor
     * 
     * @param _val
     *            - {@link Float}
     */
    public PreparedFloat(Float _val) {
        val = _val;
    }

    /**
     * {@inheritDoc}
     */
    public int addToStatement(int _startIdx, PreparedStatement _statement) throws SQLException {
        if (_statement == null)
            return _startIdx;

        if (val != null)
            _statement.setFloat(_startIdx, val);
        else
            _statement.setNull(_startIdx, Types.FLOAT);

        return ++_startIdx;
    }

    /**
     * Static method to produce an {@link InClauseColumn} for a Collection of {@link Float}s
     * 
     * @param _columnDisplay
     *            - {@link InClauseColumn} display
     * @param _values
     *            Collection of {@link Float}s
     * @return {@link InClauseColumn}
     */
    public static InClauseColumn getInClause(String _columnDisplay, Collection<Float> _values) {
        if (_values == null)
            return null;

        Queue<PreparedParameter> queueParameters = new LinkedList<PreparedParameter>();
        for (Float value : _values)
            queueParameters.add(new PreparedFloat(value));
        return new InClauseColumn(_columnDisplay, queueParameters);
    }
}
