package com.lanternsoftware.util.dao.jdbc.preparedinstatement;

import java.util.Iterator;
import java.util.Queue;

import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedParameter;

/**
 * Class that represents the data (column display and values) associated with an in-clause column Example: Batch Size: 2
 * select * from table where (record_id, str_val) in (('1','2'),('3','4')) InClauseColumn is an abstract representation
 * for the pieces of information about a specific column within the in-clause: InClauseColumn Column Display: record_id
 * Parameters: '1', '3' InClauseColumn Column Display: str_val Parameters: '2', '4'
 */
public class InClauseColumn {
    private final String sqlColumnDisplay;
    private final Queue<? extends PreparedParameter> parameters;
    private final int size;
    private Iterator<? extends PreparedParameter> currentParam;

    /**
     * Default Constructor
     * 
     * @param _sqlColumnDisplay
     *            - String representing the column's display. Note that this may not just be the column name, but may also
     *            represent the column's alias: 'record_id' or 't.record_id'.
     * @param _parameters
     *            - a {@link Queue} of {@link PreparedParameter}.
     */
    public InClauseColumn(String _sqlColumnDisplay, final Queue<? extends PreparedParameter> _parameters) {
        sqlColumnDisplay = _sqlColumnDisplay;
        if (_parameters != null) {
            parameters = _parameters;
            size = parameters.size();
            currentParam = parameters.iterator();
        }
        else {
            parameters = null;
            size = 0;
            currentParam = null;
        }
    }

    /**
     * @return String representing the column's display
     */
    public String getColumnDisplay() {
        return sqlColumnDisplay;
    }

    /**
     * @return boolean; true if there are remaining {@link PreparedParameter}s in the queue
     */
    public boolean hasNextParameter() {
        if (parameters == null)
            return false;

        return currentParam.hasNext();
    }

    /**
     * @return the next {@link PreparedParameter}s in the queue; may return null if queue is empty
     */
    public PreparedParameter getNextParameter() {
        if (currentParam == null)
            return null;

        return currentParam.next();
    }

    /**
     * @return Integer representing the initial size of the queue (not current size)
     */
    public int getParameterCnt() {
        return size;
    }

    /**
     * Method that allows you re-use the column. This method will reset the {@link #hasNextParameter()} and
     * {@link #getNextParameter()} methods.
     */
    public void reset() {
        if (parameters != null)
            currentParam = parameters.iterator();
        else
            currentParam = null;
    }
}
