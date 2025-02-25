package com.lanternsoftware.util.dao.jdbc.preparedparameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.regex.Pattern;

import com.lanternsoftware.util.dao.jdbc.preparedinstatement.InClauseColumn;

/**
 * Implementation of {@link PreparedParameter} for {@link String}s
 */
public class PreparedString implements PreparedParameter {
    private final String val;
    private final boolean isKey;
    private static final Pattern KEY_PATTERN = Pattern.compile("[^A-Za-z0-9]+");

    /**
     * Default Constructor
     * 
     * @param _val
     *            - {@link String}
     */
    public PreparedString(String _val) {
        this(_val, false);
    }

    /**
     * Default Constructor
     * 
     * @param _val
     *            - {@link String}
     * @param _isKey
     *            - boolean; if true the {@link String} should be converted to upper case alpha numeric before being saved
     *            the {@link PreparedStatement}
     */
    public PreparedString(String _val, boolean _isKey) {
        val = _val;
        isKey = _isKey;
    }

    /**
     * {@inheritDoc}
     */
    public int addToStatement(int _startIdx, PreparedStatement _statement) throws SQLException {
        if (_statement == null)
            return _startIdx;

        if (val != null) {
            if (isKey) {
                String upper = val.toUpperCase(Locale.ENGLISH);
                _statement.setString(_startIdx, KEY_PATTERN.matcher(upper).replaceAll(""));
            }
            else
                _statement.setString(_startIdx, val);
        }
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
    public static InClauseColumn getInClause(String _columnDisplay, Collection<String> _values) {
        return getInClause(_columnDisplay, _values, false);
    }

    /**
     * Static method to produce an {@link InClauseColumn} for a Collection of {@link String}s
     * 
     * @param _columnDisplay
     *            - {@link InClauseColumn} display
     * @param _values
     *            - Collection of {@link String}s
     * @param _isKeyColumn
     *            - boolean; if true the {@link String} should be converted to upper case alpha numeric before being saved
     *            Collection
     * @return {@link InClauseColumn}
     */
    public static InClauseColumn getInClause(String _columnDisplay, Collection<String> _values, boolean _isKeyColumn) {
        if (_values == null)
            return null;

        Queue<PreparedParameter> queueParameters = new LinkedList<PreparedParameter>();
        for (String value : _values)
            queueParameters.add(new PreparedString(value, _isKeyColumn));
        return new InClauseColumn(_columnDisplay, queueParameters);
    }
}
