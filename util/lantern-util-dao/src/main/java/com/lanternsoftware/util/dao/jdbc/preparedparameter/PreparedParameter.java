package com.lanternsoftware.util.dao.jdbc.preparedparameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface representing a {@link PreparedStatement}'s parameters
 */
public interface PreparedParameter {
    /**
     * Method that will build add the parameter to a {@link PreparedStatement}
     * 
     * @param _startIdx
     *            - integer representing the starting index of the parameter
     * @param _statement
     *            - {@link PreparedStatement}
     * @return an int, representing the ending index of the parameter
     * @throws {@link
     *             SQLException}
     */
    int addToStatement(int _startIdx, PreparedStatement _statement) throws SQLException;
}
