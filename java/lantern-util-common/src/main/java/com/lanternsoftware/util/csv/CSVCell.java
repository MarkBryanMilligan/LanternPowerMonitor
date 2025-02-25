package com.lanternsoftware.util.csv;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.lanternsoftware.util.NullUtils;

public class CSVCell implements Comparable<CSVCell> {
    public final String display;
    public final Object comparable;
    public final boolean reverseSort;
    
    public CSVCell(String _display) {
        this(_display, _display);
    }
    
    public CSVCell(String _display, Object _comparable) {
        this(_display, _comparable, false);
    }
    
    public CSVCell(String _display, Object _comparable, boolean _reverseSort) {
        display = _display;
        comparable = _comparable;
        reverseSort = _reverseSort;
    }
    
    @Override
    public int compareTo(CSVCell _o) {
        if (_o == null)
            return 1;
        Object type = (comparable == null) ? _o.comparable : comparable;
        if (type instanceof String)
            return NullUtils.compare((String) comparable, (String) _o.comparable, false);
        if (type instanceof Date)
            return NullUtils.compare((Date) comparable, (Date) _o.comparable, false);
        if (type instanceof Integer)
            return NullUtils.compare((Integer) comparable, (Integer) _o.comparable, false);
        if (type instanceof Long)
            return NullUtils.compare((Long) comparable, (Long) _o.comparable, false);
        if (type instanceof Double)
            return NullUtils.compare((Double) comparable, (Double) _o.comparable, false);
        if (type instanceof Boolean)
            return NullUtils.compare((Boolean) comparable, (Boolean) _o.comparable, false);
        if (type instanceof Float)
            return NullUtils.compare((Float) comparable, (Float) _o.comparable, false);
        return 0;
    }

    public static List<CSVCell> asList(String... _data) {
        if (_data == null)
            return new ArrayList<CSVCell>(0);
        List<CSVCell> listCells = new ArrayList<CSVCell>(_data.length);
        for (String data : _data) {
            listCells.add(new CSVCell(data));
        }
        return listCells;
    }
}
