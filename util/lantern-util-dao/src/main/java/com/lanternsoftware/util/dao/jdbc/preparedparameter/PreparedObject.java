package com.lanternsoftware.util.dao.jdbc.preparedparameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import com.lanternsoftware.util.dao.jdbc.preparedinstatement.InClauseColumn;

/**
 * Implementation of {@link PreparedParameter} for {@link Short}s
 */
public class PreparedObject implements PreparedParameter {
    private final Object m_val;

    public PreparedObject(Object _val) {
        m_val = _val;
    }

    public int addToStatement(int _startIdx, PreparedStatement _statement) throws SQLException {
        if (_statement == null)
            return _startIdx;

        if (m_val != null)
            _statement.setObject(_startIdx, m_val);
        else
            _statement.setNull(_startIdx, Types.OTHER);

        return ++_startIdx;
    }

    public static InClauseColumn getInClause(String _columnDisplay, Collection<Object> _values) {
        if (_values == null)
            return null;

        Queue<PreparedParameter> queueParameters = new LinkedList<PreparedParameter>();
        for (Object value : _values)
            queueParameters.add(new PreparedObject(value));
        return new InClauseColumn(_columnDisplay, queueParameters);
    }
}
