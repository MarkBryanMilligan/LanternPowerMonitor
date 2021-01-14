package com.lanternsoftware.util.dao.jdbc.preparedparameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import com.lanternsoftware.util.dao.jdbc.preparedinstatement.InClauseColumn;

/**
 * Implementation of {@link PreparedParameter} for {@link Boolean}s
 */
public class PreparedBoolean implements PreparedParameter {
    private final Boolean val;

    public PreparedBoolean(Boolean _val) {
        val = _val;
    }

    /**
     * {@inheritDoc}
     */
    public int addToStatement(int _startIdx, PreparedStatement _statement) throws SQLException {
        if (_statement == null)
            return _startIdx;

        if (val != null)
            _statement.setBoolean(_startIdx, val);
        else
            _statement.setNull(_startIdx, Types.TINYINT);

        return ++_startIdx;
    }

    /**
     * Static method to produce an {@link InClauseColumn} for a Collection of {@link Boolean}s
     * 
     * @param _columnDisplay
     *            - {@link InClauseColumn} display
     * @param _values
     *            Collection of {@link Boolean}s
     * @return {@link InClauseColumn}
     */
    public static InClauseColumn getInClause(String _columnDisplay, Collection<Boolean> _values) {
        if (_values == null)
            return null;

        Queue<PreparedParameter> queueParameters = new LinkedList<PreparedParameter>();
        for (Boolean value : _values)
            queueParameters.add(new PreparedBoolean(value));
        return new InClauseColumn(_columnDisplay, queueParameters);
    }
}
