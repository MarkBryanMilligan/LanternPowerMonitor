package com.lanternsoftware.util.dao.jdbc.preparedparameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Queue;

import com.lanternsoftware.util.dao.jdbc.preparedinstatement.InClauseColumn;

/**
 * Implementation of {@link PreparedParameter} for {@link Long}s
 */
public class PreparedLong implements PreparedParameter {
    private final Long val;

    /**
     * Constructor taking a long value
     * 
     * @param _val
     *            - {@link Long}
     */
    public PreparedLong(Long _val) {
        val = _val;
    }

    /**
     * Constructor taking a GregorianCalendar that will be converted to a long using the
     * {@link GregorianCalendar#getTimeInMillis()} value.
     * 
     * @param _cal
     *            - {@link GregorianCalendar}
     */
    public PreparedLong(GregorianCalendar _cal) {
        if (_cal == null)
            val = null;
        else
            val = _cal.getTimeInMillis();
    }

    /**
     * {@inheritDoc}
     */
    public int addToStatement(int _startIdx, PreparedStatement _statement) throws SQLException {
        if (_statement == null)
            return _startIdx;

        if (val != null)
            _statement.setLong(_startIdx, val);
        else
            _statement.setNull(_startIdx, Types.BIGINT);

        return ++_startIdx;
    }

    /**
     * Static method to produce an {@link InClauseColumn} for a Collection of {@link Long}s
     * 
     * @param _columnDisplay
     *            - {@link InClauseColumn} display
     * @param _values
     *            Collection of {@link Long}s
     * @return {@link InClauseColumn}
     */
    public static InClauseColumn getInClause(String _columnDisplay, Collection<Long> _values) {
        if (_values == null)
            return null;

        Queue<PreparedParameter> queueParameters = new LinkedList<PreparedParameter>();
        for (Long value : _values)
            queueParameters.add(new PreparedLong(value));
        return new InClauseColumn(_columnDisplay, queueParameters);
    }

    /**
     * Static method to produce an {@link InClauseColumn} for a Collection of {@link GregorianCalendar}s. The
     * GregorianCalendar will be converted to a {@link Long} using the {@link GregorianCalendar#getTimeInMillis()} value.
     * 
     * @param _columnDisplay
     *            - {@link InClauseColumn} display
     * @param _values
     *            Collection of {@link GregorianCalendar}s
     * @return {@link InClauseColumn}
     */
    public static InClauseColumn getInClauseCalendars(String _columnDisplay, Collection<GregorianCalendar> _values) {
        if (_values == null)
            return null;

        Queue<PreparedParameter> queueParameters = new LinkedList<PreparedParameter>();
        for (GregorianCalendar value : _values)
            queueParameters.add(new PreparedLong(value));
        return new InClauseColumn(_columnDisplay, queueParameters);
    }
}
