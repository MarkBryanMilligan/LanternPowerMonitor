package com.lanternsoftware.util.dao.jdbc.preparedparameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import com.lanternsoftware.util.dao.jdbc.preparedinstatement.InClauseColumn;

/**
 * Implementation of {@link PreparedParameter} for {@link Integer}s
 */
public class PreparedInt implements PreparedParameter {
    private final Integer val;

    /**
     * Default Constructor
     * 
     * @param _val
     *            - {@link Integer}
     */
    public PreparedInt(Integer _val) {
        val = _val;
    }

    /**
     * {@inheritDoc}
     */
    public int addToStatement(int _startIdx, PreparedStatement _statement) throws SQLException {
        if (_statement == null)
            return _startIdx;

        if (val != null)
            _statement.setInt(_startIdx, val);
        else
            _statement.setNull(_startIdx, Types.INTEGER);

        return ++_startIdx;
    }

    /**
     * Static method to produce an {@link InClauseColumn} for a Collection of {@link Integer}s
     * 
     * @param _columnDisplay
     *            - {@link InClauseColumn} display
     * @param _values
     *            Collection of {@link Integer}s
     * @return {@link InClauseColumn}
     */
    public static InClauseColumn getInClause(String _columnDisplay, Collection<Integer> _values) {
        if (_values == null)
            return null;

        Queue<PreparedParameter> queueParameters = new LinkedList<PreparedParameter>();
        for (Integer value : _values)
            queueParameters.add(new PreparedInt(value));
        return new InClauseColumn(_columnDisplay, queueParameters);
    }

    /**
     * Static method to produce an {@link InClauseColumn} for a Collection of {@link Enum}s. The Integer value will be
     * generated for each enum by using the enum's {@link Enum#ordinal()} value.
     * 
     * @param _columnDisplay
     *            - {@link InClauseColumn} display
     * @param _values
     *            Collection of {@link Enum}s
     * @return {@link InClauseColumn} containing integers
     */
    public static InClauseColumn getInClauseEnums(String _columnDisplay, Collection<? extends Enum<?>> _values) {
        if (_values == null)
            return null;

        Queue<PreparedParameter> queueParameters = new LinkedList<PreparedParameter>();
        for (Enum<?> value : _values) {
            if (value != null)
                queueParameters.add(new PreparedInt(value.ordinal()));
            else
                queueParameters.add(new PreparedInt(null));
        }
        return new InClauseColumn(_columnDisplay, queueParameters);
    }
}
