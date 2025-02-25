package com.lanternsoftware.util.dao.jdbc.preparedinstatement;

import java.sql.PreparedStatement;
import java.util.LinkedList;

import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedParameter;

/**
 * Class that represents a collection of data to be set into a {@link PreparedStatement} ({@link PreparedParameter}).
 * This will represent all of the data associated with an {@link InClauseColumn} Example: Batch Size: 2 select * from
 * table where (record_id, str_val) in (('1','2'),('3','4')) InClauseBatchedParameter represents the Collection of
 * batched parameters need to execute the InClause: Collection<PreparedParameter>: '1', '2', '3', '4' If we change the
 * batch size to one, the InClause would now have 2 InClauseBatchedParameters: Collection<PreparedParameter>: '1', '3'
 * Collection<PreparedParameter>: '2', '4'
 */
public class InClauseBatchedParameter {
    private final LinkedList<PreparedParameter> parameters = new LinkedList<PreparedParameter>();
    private final InClauseColumn[] columns;

    private int size = 0;
    private boolean returnAllIfEmpty = false;

    /**
     * Default Constructor
     * 
     * @param _arrColumns
     *            - a variable list of {@link InClauseColumn}s that make up the in-clause
     */
    public InClauseBatchedParameter(InClauseColumn[] _arrColumns) {
        columns = _arrColumns;
    }

    /**
     * Method to add a new primitive parameter to the batched Collection
     * 
     * @param _parameter
     *            - the {@link PreparedParameter} to be added
     */
    public void addParameter(PreparedParameter _parameter) {
        parameters.add(_parameter);
        ++size;
    }

    /**
     * @param _bReturnAllIfEmpty
     *            - a boolean flag that determines the resulting behavior if the {@link InClauseColumn}s are empty. If true,
     *            the in-clause will be replaced with a '1=1', meaning all rows would be returned; false will replace with
     *            '1 = 0'
     */
    public void setReturnAllIfEmpty(boolean _bReturnAllIfEmpty) {
        returnAllIfEmpty = _bReturnAllIfEmpty;
    }

    /**
     * @return boolean; if true, the in-clause will be replaced with a '1=1', meaning all rows would be returned; false will
     *         replace with '1 = 0'
     */
    public boolean isReturnAllIfEmpty() {
        return returnAllIfEmpty;
    }

    /**
     * @return Integer representing the number of batched {@link PreparedParameter}s
     */
    public int getBatchParameterCnt() {
        return size;
    }

    /**
     * @return Integer representing the number of {@link InClauseColumn}s that make up the batched
     *         {@link PreparedParameter}s
     */
    public int getColumnCnt() {
        return columns.length;
    }

    /**
     * @return the array of {@link InClauseColumn}s that make up the batched {@link PreparedParameter}s
     */
    public InClauseColumn[] getColumns() {
        return columns;
    }

    /**
     * @return the {@link LinkedList} of batched {@link PreparedParameter}s
     */
    public LinkedList<PreparedParameter> getParameters() {
        return parameters;
    }
}
