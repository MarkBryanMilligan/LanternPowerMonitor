package com.lanternsoftware.util.dao.jdbc.preparedinstatement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import com.lanternsoftware.util.dao.jdbc.preparedparameter.PreparedParameter;

/**
 * Class that represents an instance of batched parameters for all {@link InClause}s that are part of an SQL statement.
 * Example: Batch Size: 2 select * from table where (record_id) in ('A','B','C') and str_val in('Y','Z') and dbl_val
 * in(1, 2, 3) In this case, we will end up with 4 distinct statements to represent the data within the 3 in-clauses:
 * Statement 1: Query: select * from table where (record_id) in (?,?) and str_val = in(?,?) and dbl_val in(?,?)
 * Parameters: 1. Collection<PreparedParameter>: Parameters: 'A', 'B' 2. Collection<PreparedParameter>: Parameters: 'Y',
 * 'Z' 3. Collection<PreparedParameter>: Parameters: 1, 2 Statement 2: Query: select * from table where record_id = ?
 * and str_val = in(?,?) and dbl_val in(?,?) Parameters: 1. Collection<PreparedParameter>: Parameters: 'C' 2.
 * Collection<PreparedParameter>: Parameters: 'Y', 'Z' 3. Collection<PreparedParameter>: Parameters: 1, 2 Statement 3:
 * Query: select * from table where (record_id) in (?,?) and str_val = in(?,?) and dbl_val = ? Parameters: 1.
 * Collection<PreparedParameter>: Parameters: 'A', 'B' 2. Collection<PreparedParameter>: Parameters: 'Y', 'Z' 3.
 * Collection<PreparedParameter>: Parameters: 3 Statement 4: Query: select * from table where (record_id) = ? and
 * str_val = in(?,?) and dbl_val = ? Parameters: 1. Collection<PreparedParameter>: Parameters: 'C' 2.
 * Collection<PreparedParameter>: Parameters: 'Y', 'Z' 3. Collection<PreparedParameter>: Parameters: 3
 */
public class InClauseStatement implements Cloneable {
    public static final String IN_CLAUSE_EXPRESSION = "{in}";

    private static final String IN_CLAUSE_REGEX = "\\{in\\}";
    private static final String QUESTION_MARK = "?";
    private static final String COMMA = ",";
    private static final String LEFT_PARANTHESIS = "(";
    private static final String RIGHT_PARANTHESIS = ")";
    private static final String IN_STATEMENT = " in ";
    private static final String EQUALS = " = ";
    private static final String SQL_NOT_EQUAL = "0 = 1";
    private static final String SQL_EQUAL = "1 = 1";

    private Map<Integer, InClauseBatchedParameter> parameters = new HashMap<Integer, InClauseBatchedParameter>();

    /**
     * Method that sets a {@link InClauseBatchedParameter} (representing the data from an {@link InClause} for a specific
     * index
     *
     * @param _startIdx
     *            - Integer representing the {@link InClause}'s index
     * @param _parameter
     *            - {@link InClauseBatchedParameter} (representing the data from an {@link InClause}
     */
    void setNextParameter(int _startIdx, InClauseBatchedParameter _parameter) {
        if (_parameter == null)
            return;

        parameters.put(_startIdx, _parameter);
    }

    /**
     * {@inheritDoc}
     */
    public InClauseStatement clone() {
        InClauseStatement statement = new InClauseStatement();

        Iterator<Entry<Integer, InClauseBatchedParameter>> iter = parameters.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Integer, InClauseBatchedParameter> entry = iter.next();
            if (entry == null)
                continue;

            statement.setNextParameter(entry.getKey(), entry.getValue());
        }

        return statement;
    }

    /**
     * @return a Map of index to {@link InClauseBatchedParameter} (representing the data from an {@link InClause}
     */
    Map<Integer, InClauseBatchedParameter> getParameters() {
        return parameters;
    }

    /**
     * Method to generate the column display for an in-clause. Depending on the number of columns, the format may vary: 1
     * column: (column) 2 or more columns: (column_one, column_two)
     *
     * @param _arrColumns
     *            - An Array of {@link InClauseColumn}s needed to build the display
     * @return String representing the in-clause output
     */
    String buildColumnDisplay(InClauseColumn[] _arrColumns) {
        StringBuilder sbColumns = new StringBuilder();
        for (InClauseColumn column : _arrColumns) {
            if (column == null)
                continue;

            if (sbColumns.length() > 0)
                sbColumns.append(COMMA);
            sbColumns.append(column.getColumnDisplay());
        }

        StringBuilder sbDisplay = new StringBuilder();
        sbDisplay.append(LEFT_PARANTHESIS);
        sbDisplay.append(sbColumns.toString());
        sbDisplay.append(RIGHT_PARANTHESIS);
        sbDisplay.append(IN_STATEMENT);
        return sbDisplay.toString();
    }

    /**
     * Generates a column display for special cases where there is only 1 column. <strong>Special Case:</strong> A single
     * {@link InClauseColumn} with a single {@link PreparedParameter}s is set. In this situation, the in-clause will be
     * optimized to be an equals (i.e. = ? and not in (?)). The string will format to: column =
     *
     * @param _columns
     * @return
     */
    String buildEqualDisplay(InClauseColumn[] _columns) {
        StringBuilder sbDisplay = new StringBuilder();
        int nLength = _columns.length;

        // special case: if there is only 1 column with 1 value in the parameter, we will format the
        // select as column = ?, rather than column in (?)
        if (nLength == 1) {
            sbDisplay.append(_columns[0].getColumnDisplay());
            sbDisplay.append(EQUALS);
        }
        return sbDisplay.toString();
    }

    /**
     * Method to generate a dynamic SQL-query from an SQL query using in-clause delimiters (i.e {in}). Example: Batch Size:
     * 2 select * from table where {in} and {in} and {in} In-Clause 1 Data: record_id - 'A','B','C' In-Clause 2 Data:
     * str_val - 'Y','Z' In-Clause 3 Data: dbl_val - 1, 2, 3 Will return the following formatted queries: Statement 1:
     * Query: select * from table where (record_id) in (?,?) and str_val = in(?,?) and dbl_val in(?,?) Parameters: 1.
     * Collection<PreparedParameter>: Parameters: 'A', 'B' 2. Collection<PreparedParameter>: Parameters: 'Y', 'Z' 3.
     * Collection<PreparedParameter>: Parameters: 1, 2 Statement 2: Query: select * from table where record_id = ? and
     * str_val = in(?,?) and dbl_val in(?,?) Parameters: 1. Collection<PreparedParameter>: Parameters: 'C' 2.
     * Collection<PreparedParameter>: Parameters: 'Y', 'Z' 3. Collection<PreparedParameter>: Parameters: 1, 2 Statement 3:
     * Query: select * from table where (record_id) in (?,?) and str_val = in(?,?) and dbl_val = ? Parameters: 1.
     * Collection<PreparedParameter>: Parameters: 'A', 'B' 2. Collection<PreparedParameter>: Parameters: 'Y', 'Z' 3.
     * Collection<PreparedParameter>: Parameters: 3 Statement 4: Query: select * from table where (record_id) = ? and
     * str_val = in(?,?) and dbl_val = ? Parameters: 1. Collection<PreparedParameter>: Parameters: 'C' 2.
     * Collection<PreparedParameter>: Parameters: 'Y', 'Z' 3. Collection<PreparedParameter>: Parameters: 3
     *
     * @param _query
     *            - String representing the in-clause delimited SQL statement
     * @return a String representing the dynamically modified SQL statement
     */
    String formatQuery(String _query) {
        if (parameters.size() == 0)
            return _query;

        String sModifiedQuery = _query;
        Iterator<InClauseBatchedParameter> iter = parameters.values().iterator();
        while (iter.hasNext()) {
            InClauseBatchedParameter parameter = iter.next();
            if (parameter == null)
                return "";

            int nColumnCnt = parameter.getColumnCnt();
            int nCurColumnCnt = 0;

            StringBuilder sbParameters = new StringBuilder();

            // special case: if there are no parameters, check to see if we should return a true or false for the in statement
            // i.e. 0 = 1 or 1 = 1
            if (parameter.getBatchParameterCnt() == 0) {
                if (parameter.isReturnAllIfEmpty())
                    sbParameters.append(SQL_EQUAL);
                else
                    sbParameters.append(SQL_NOT_EQUAL);
            }

            // special case: if there is only 1 column with 1 value in the parameter, we will format the
            // select as column = ?, rather than column in (?)
            else if (nColumnCnt == 1 && parameter.getBatchParameterCnt() == 1) {
                sbParameters.append(buildEqualDisplay(parameter.getColumns()));
                sbParameters.append(QUESTION_MARK);
            }

            // normal replacement case
            else {

                sbParameters.append(buildColumnDisplay(parameter.getColumns()));
                sbParameters.append(LEFT_PARANTHESIS);

                if (nColumnCnt > 1)
                    sbParameters.append(LEFT_PARANTHESIS);

                for (int nCurIdx = 0; nCurIdx < parameter.getBatchParameterCnt(); ++nCurIdx) {
                    if (nColumnCnt > 1 && nCurColumnCnt == nColumnCnt) {
                        sbParameters.append(RIGHT_PARANTHESIS);
                        sbParameters.append(COMMA);
                        sbParameters.append(LEFT_PARANTHESIS);
                        nCurColumnCnt = 0;
                    }
                    else if (nCurIdx > 0)
                        sbParameters.append(COMMA);
                    sbParameters.append(QUESTION_MARK);
                    ++nCurColumnCnt;
                }

                if (nColumnCnt > 1)
                    sbParameters.append(RIGHT_PARANTHESIS);

                sbParameters.append(RIGHT_PARANTHESIS);
            }

            sModifiedQuery = sModifiedQuery.replaceFirst(IN_CLAUSE_REGEX, sbParameters.toString());
        }
        return sModifiedQuery;
    }
}
