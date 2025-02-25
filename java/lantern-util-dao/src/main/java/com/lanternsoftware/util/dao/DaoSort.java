package com.lanternsoftware.util.dao;

import java.util.ArrayList;
import java.util.List;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.ITransformer;
import com.lanternsoftware.util.dao.annotations.CaseFormat;

public class DaoSort {
    private final List<DaoSortField> fields = new ArrayList<DaoSortField>();

    public DaoSort then(String _field) {
        return then(_field, true);
    }

    public DaoSort thenIgnoreCase(String _field) {
        return then(_field, true, true);
    }

    public DaoSort thenDesc(String _field) {
        return then(_field, false);
    }

    public DaoSort thenDescIgnoreCase(String _field) {
        return then(_field, false, true);
    }

    public DaoSort then(String _field, boolean _ascending) {
        return then(_field, _ascending, false);
    }

    public DaoSort then(String _field, boolean _ascending, boolean _ignoreCase) {
        fields.add(new DaoSortField(_field, _ascending, false));
        return this;
    }

    public static DaoSort sort(String _field) {
        return new DaoSort().then(_field);
    }

    public static DaoSort sortIgnoreCase(String _field) {
        return new DaoSort().thenIgnoreCase(_field);
    }

    public static DaoSort sortDesc(String _field) {
        return new DaoSort().thenDesc(_field);
    }

    public static DaoSort sortDescIgnoreCase(String _field) {
        return new DaoSort().thenDescIgnoreCase(_field);
    }

    public List<DaoSortField> getFields() {
        return fields;
    }

    public static DaoSort fromQueryParams(List<String> _queryParams) {
        return fromQueryParams(_queryParams, CaseFormat.CAMEL, CaseFormat.SNAKE);
    }

    public static DaoSort fromQueryParams(List<String> _queryParams, final CaseFormat _paramFormat, final CaseFormat _dbFormat) {
        DaoSort sort = new DaoSort();
        sort.fields.addAll(CollectionUtils.transform(_queryParams, new ITransformer<String, DaoSortField>() {
            @Override
            public DaoSortField transform(String _s) {
                return DaoSortField.fromQueryParam(_s, _paramFormat, _dbFormat);
            }
        }));
        return sort;
    }
}
