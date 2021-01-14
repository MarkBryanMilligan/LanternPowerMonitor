package com.lanternsoftware.util.dao.jdbc.preparedparameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Queue;

import com.lanternsoftware.util.dao.jdbc.preparedinstatement.InClauseColumn;

/**
 * Implementation of {@link PreparedParameter} for {@link String}s
 */
public class PreparedEnum implements PreparedParameter {
    private final Enum<?> val;

    public PreparedEnum(Enum<?> _val) {
        val = _val;
    }

    /**
     * {@inheritDoc}
     */
    public int addToStatement(int _startIdx, PreparedStatement _statement) throws SQLException {
        if (_statement == null)
            return _startIdx;

        if (val != null)
            _statement.setString(_startIdx, val.name());
        else
            _statement.setNull(_startIdx, Types.VARCHAR);

        return ++_startIdx;
    }

    /**
     * Static method to produce an {@link InClauseColumn} for a Collection of {@link String}s
     * 
     * @param _columnDisplay
     *            - {@link InClauseColumn} display
     * @param _values
     *            - Collection of {@link String}s
     * @return {@link InClauseColumn}
     */
    public static InClauseColumn getInClause(String _columnDisplay, Collection<Enum<?>> _values) {
        if (_values == null)
            return null;

        Queue<PreparedParameter> queueParameters = new LinkedList<PreparedParameter>();
        for (Enum<?> value : _values)
            queueParameters.add(new PreparedEnum(value));
        return new InClauseColumn(_columnDisplay, queueParameters);
    }

    /**
     * Static method to produce an {@link InClauseColumn} for a Collection of {@link String}s
     * 
     * @param _columnDisplay
     *            - {@link InClauseColumn} display
     * @param _values
     *            - Collection of {@link String}s
     * @return {@link InClauseColumn}
     */
    public static InClauseColumn getInClause(String _columnDisplay, EnumSet<?> _values) {
        if (_values == null)
            return null;

        Queue<PreparedParameter> queueParameters = new LinkedList<PreparedParameter>();
        for (Enum<?> value : _values)
            queueParameters.add(new PreparedEnum(value));
        return new InClauseColumn(_columnDisplay, queueParameters);
    }
}
