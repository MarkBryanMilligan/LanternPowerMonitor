package com.lanternsoftware.util.csv;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;

import java.util.ArrayList;
import java.util.List;

public class CSV {
    public final int columns;
    public final int rows;
    
    private final List<String> header;
    private final List<CSVCell[]> data;
    
    public CSV() {
        this(null, null, 0);
    }
    
    public CSV(List<String> _header, List<List<String>> _data, int _columnCount) {
        header = _header;
        if (_data == null) {
            columns = 0;
            rows = 0;
            data = new ArrayList<>(0);
        }
        else {
            rows = _data.size();
            columns = _columnCount;
            data = new ArrayList<>(rows);
            for (List<String> listSourceRow : _data) {
                CSVCell[] row = new CSVCell[columns];
                int iCol = 0;
                for (String sCol : CollectionUtils.makeNotNull(listSourceRow)) {
                    row[iCol] = new CSVCell(sCol, sCol);
                    iCol++;
                }
                while (iCol < columns) {
                    row[iCol] = new CSVCell("", "");
                    iCol++;
                }
                data.add(row);
            }
        }
    }
    
    public CSV(List<String> _header, List<List<CSVCell>> _data) {
        header = _header;
        if (_data == null) {
            columns = 0;
            rows = 0;
            data = new ArrayList<>(0);
        }
        else {
            rows = _data.size();
            data = new ArrayList<>(rows);
            int iMaxColumn = CollectionUtils.size(header);
            for (List<CSVCell> listSourceRow : _data) {
                iMaxColumn = Math.max(iMaxColumn, CollectionUtils.size(listSourceRow));
            }
            columns = iMaxColumn;
            for (List<CSVCell> listSourceRow : _data) {
                CSVCell[] row = new CSVCell[columns];
                int iCol = 0;
                for (CSVCell cell : CollectionUtils.makeNotNull(listSourceRow)) {
                    row[iCol] = cell == null ? new CSVCell("", "") : cell;
                    iCol++;
                }
                while (iCol < columns) {
                    row[iCol] = new CSVCell("", "");
                    iCol++;
                }
                data.add(row);
            }
        }
    }
    
    public String cell(int _row, int _column) {
        if ((_row < 0) || (_row >= rows) || (_column < 0) || (_column >= columns))
            return "";
        CSVCell cell = data.get(_row)[_column];
        if ((cell == null) || (cell.display == null))
            return "";
        return cell.display;
    }
    
    public List<String> getHeaders() {
        return header;
    }
    
    public String getHeader(int _column) {
        return NullUtils.makeNotNull(CollectionUtils.get(header, _column));
    }
    
    public int getRows() {
        return rows;
    }
    
    public int getColumns() {
        return columns;
    }
}
