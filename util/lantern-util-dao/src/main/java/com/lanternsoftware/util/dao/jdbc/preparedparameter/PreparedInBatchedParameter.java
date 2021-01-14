package com.lanternsoftware.util.dao.jdbc.preparedparameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

import com.lanternsoftware.util.dao.jdbc.preparedinstatement.InClauseBatchedParameter;

/**
 * Implementation of {@link PreparedParameter} for {@link InClauseBatchedParameter}s
 */
public class PreparedInBatchedParameter implements PreparedParameter {
    private final InClauseBatchedParameter batchedParameter;

    /**
     * Default Constructor
     * 
     * @param _parameter
     *            - {@link InClauseBatchedParameter}
     */
    public PreparedInBatchedParameter(InClauseBatchedParameter _parameter) {
        batchedParameter = _parameter;
    }

    /**
     * {@inheritDoc}
     */
    public int addToStatement(int _startIdx, PreparedStatement _statement) throws SQLException {
        if (_statement == null || batchedParameter == null)
            return _startIdx;

        LinkedList<PreparedParameter> listParameters = batchedParameter.getParameters();
        if (listParameters == null || listParameters.isEmpty())
            return _startIdx;

        int nIdx = _startIdx;
        for (PreparedParameter parameter : listParameters) {
            if (parameter == null)
                continue;
            parameter.addToStatement(nIdx, _statement);
            ++nIdx;
        }

        return nIdx;
    }
}
