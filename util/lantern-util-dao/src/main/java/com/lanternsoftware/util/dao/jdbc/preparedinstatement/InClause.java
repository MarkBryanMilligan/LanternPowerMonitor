package com.lanternsoftware.util.dao.jdbc.preparedinstatement;

import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.LinkedList;

import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedParameter;

/**
 * Class that represents a single in-clause statement (all columns - {@link InClauseColumn}) Example: Batch Size: 2
 * select * from table where (record_id, str_val) in (('1','2'),('3','4')) InClause is an abstract representation for
 * all of the pieces of information: InClauseColumn Column Display: record_id Parameters: '1', '3' InClauseColumn Column
 * Display: str_val Parameters: '2', '4'
 */
class InClause {
    private final InClauseColumn[] columns;
    private BatchBucket batchBucket;
    private final int startIdx;
    private final boolean returnAllIfEmpty;

    /**
     * Default Constructor
     * 
     * @param _startIdx
     *            - Integer representing the index of the in-clause inside the SQL statement
     * @param _returnAllIfEmpty
     *            - a boolean flag that determines the resulting behavior if the {@link InClauseColumn}s are empty. If true,
     *            the in-clause will be replaced with a '1=1', meaning all rows would be returned; false will replace with
     *            '1 = 0'.
     * @param _columns
     *            - a variable list of {@link InClauseColumn}s that make up the in-clause
     */
    public InClause(int _startIdx, boolean _returnAllIfEmpty, InClauseColumn... _columns) {
        columns = _columns;
        startIdx = _startIdx;
        returnAllIfEmpty = _returnAllIfEmpty;
        batchBucket = new BatchBucket(0);
    }

    /**
     * Method which defines a maximum batch size used to calculate in-clause batch sizes.
     * 
     * @param _batchSize
     */
    public void setMaxBatchSize(int _batchSize) {
        batchBucket = new BatchBucket(_batchSize);
    }

    /**
     * Method to take all of the {@link InClauseColumn}s that make up the in-clause and turned those into a list of
     * {@link InClauseBatchedParameter}s. When executing an in-clause, some databases restrict the number of items that may
     * be in a specified in-clause (currently the limit is 1000). This item-limit includes items for all columns (i.e. if
     * you have two columns, each column may only contribute half of the total - i.e. 500 items). Therefore, we need to
     * build a list of batched parameters that do not exceed the maximum item threshold, but at the same time is batched
     * appropriately (to minimize the number of {@link PreparedStatement}s that will be generated. When optimizing batch
     * sizes, there are a few special cases that are noted below. <strong>Special Case 1:</strong> The batch-size is less
     * than the number of {@link InClauseColumn}s In this situation, the batch size will be adjusted to the number of
     * columns. For example, if you set the batch size to 1 and you pass in 2 columns, your optimized batch size will be 2.
     * <strong>Special Case 2:</strong> No {@link PreparedParameter}s are set on any {@link InClauseColumn}s In this
     * situation, the behavior will depend on the ReturnAllIfEmpty flag passed in during the constructor. If true, the
     * in-clause will be replaced with a '1=1', meaning all rows would be returned; false will replace with '1 = 0'.
     * <strong>Special Case 3:</strong> A single {@link InClauseColumn} with a single {@link PreparedParameter}s is set. In
     * this situation, the in-clause will be optimized to be an equals (i.e. = ? and not in (?)).
     * 
     * @return a {@link LinkedList} of optimum-sized batched {@link PreparedParameter}s
     */
    public LinkedList<InClauseBatchedParameter> getBatchedParameters() {
        LinkedList<InClauseBatchedParameter> listReturnParameters = new LinkedList<InClauseBatchedParameter>();
        if (columns == null || columns.length == 0)
            return listReturnParameters;

        int nColumnCnt = columns.length;
        int nCurrentSize = 0;

        InClauseBatchedParameter batchedParameters = new InClauseBatchedParameter(columns);
        listReturnParameters.add(batchedParameters);

        // special case 2: if there are no parameters, we will honor the behavior of returnAllIfEmpty
        if (nColumnCnt >= 1 && columns[0].getParameterCnt() == 0) {
            batchedParameters.setReturnAllIfEmpty(returnAllIfEmpty);
            return listReturnParameters;
        }

        // special case 3: if there is only 1 column and 1 element in the column, we can use = , not in()
        if (nColumnCnt == 1 && columns[0].getParameterCnt() == 1) {
            batchedParameters.addParameter(columns[0].getNextParameter());
            return listReturnParameters;
        }

        InClauseBatchedParameter lastBatchedParameters = null;
        boolean bMoreParameters = true;
        while (bMoreParameters) {
            if (/* special case 1 */nCurrentSize > 0 && (nCurrentSize + nColumnCnt) > batchBucket.getMaxBatchSize()) {
                batchedParameters = new InClauseBatchedParameter(columns);
                listReturnParameters.add(batchedParameters);
                nCurrentSize = 0;
            }

            lastBatchedParameters = new InClauseBatchedParameter(columns);
            for (InClauseColumn column : columns) {
                if (column == null)
                    continue;

                PreparedParameter parameter = column.getNextParameter();
                batchedParameters.addParameter(parameter);
                lastBatchedParameters.addParameter(parameter);
                nCurrentSize++;

                if (!column.hasNextParameter())
                    bMoreParameters = false;
            }
        }

        // optimize the size of the remaining bucket
        if (lastBatchedParameters != null) {
            int nBatchSize = batchBucket.getBatchSize(nCurrentSize);
            while (nCurrentSize < nBatchSize) {
                // if we are going to add more parameters than the bucket size, we need to break now
                if (nCurrentSize + nColumnCnt > nBatchSize)
                    break;

                Iterator<PreparedParameter> iter = lastBatchedParameters.getParameters().iterator();
                while (iter.hasNext()) {
                    batchedParameters.addParameter(iter.next());
                    ++nCurrentSize;
                }
            }
        }
        return listReturnParameters;
    }

    /**
     * @return Integer representing the starting index in the SQL statement
     */
    public int getStartIndex() {
        return startIdx;
    }

    /**
     * Method to reset the clause after an evaluation
     */
    public void reset() {
        for (InClauseColumn column : columns) {
            if (column != null)
                column.reset();
        }
    }
}
