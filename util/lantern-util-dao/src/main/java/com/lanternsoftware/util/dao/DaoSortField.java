package com.lanternsoftware.util.dao;

import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.annotations.CaseFormat;

public class DaoSortField {
    private String field;
    private boolean ascending;
    private boolean ignoreCase;

    public DaoSortField() {
    }

    public DaoSortField(String _field, boolean _ascending, boolean _ignoreCase) {
        field = _field;
        ascending = _ascending;
    }

    public String getField() {
        return field;
    }

    public void setField(String _field) {
        field = _field;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean _ascending) {
        ascending = _ascending;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean _ignoreCase) {
        ignoreCase = _ignoreCase;
    }

    public static DaoSortField fromQueryParam(String _param) {
        return fromQueryParam(_param, CaseFormat.CAMEL, CaseFormat.SNAKE);
    }

    public static DaoSortField fromQueryParam(String _param, CaseFormat _paramFormat, CaseFormat _dbFormat) {
        if (NullUtils.isEmpty(_param))
            return null;
        String[] parts = _param.split(",");
        return new DaoSortField(AbstractDaoSerializer.convertCase(parts[0], _paramFormat, _dbFormat), !(parts.length > 1 && NullUtils.isEqual(parts[1], "desc")), false);
    }
}
