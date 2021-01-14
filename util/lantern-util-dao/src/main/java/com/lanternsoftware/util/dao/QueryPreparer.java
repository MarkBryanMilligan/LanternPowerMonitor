package com.lanternsoftware.util.dao;

import java.util.Map;
import java.util.Map.Entry;

import com.lanternsoftware.util.dao.annotations.CaseFormat;

public class QueryPreparer {
    private final CaseFormat queryCaseFormat;
    private final CaseFormat dbCaseFormat;
    private final Map<String, String> fieldReplacements;
    private final Map<String, String> fieldSuffixExceptions;

    public QueryPreparer(CaseFormat _queryCaseFormat, CaseFormat _dbCaseFormat) {
        this(_queryCaseFormat, _dbCaseFormat, null);
    }

    public QueryPreparer(CaseFormat _queryCaseFormat, CaseFormat _dbCaseFormat, Map<String, String> _fieldReplacements) {
        this(_queryCaseFormat, _dbCaseFormat, _fieldReplacements, null);
    }

    public QueryPreparer(CaseFormat _queryCaseFormat, CaseFormat _dbCaseFormat, Map<String, String> _fieldReplacements, Map<String, String> _fieldSuffixExceptions) {
        queryCaseFormat = _queryCaseFormat;
        dbCaseFormat = _dbCaseFormat;
        fieldReplacements = _fieldReplacements;
        fieldSuffixExceptions = _fieldSuffixExceptions;
    }

    public DaoQuery prepareQuery(DaoQuery _query) {
        DaoQuery query = new DaoQuery();
        for (Entry<String, Object> e : _query.entrySet()) {
            if (fieldReplacements != null) {
                String rep = fieldReplacements.get(e.getKey());
                if (rep != null) {
                    query.put(rep, e.getValue());
                    continue;
                }
            }
            String field = AbstractDaoSerializer.convertCase(e.getKey(), queryCaseFormat, dbCaseFormat);
            if (fieldSuffixExceptions != null) {
                for (Entry<String, String> entry : fieldSuffixExceptions.entrySet()) {
                    if (field.endsWith(entry.getKey()))
                        field = field.substring(0, field.length()-entry.getKey().length()) + entry.getValue();
                }
            }
            query.put(field, e.getValue());
        }
        return query;
    }
}
