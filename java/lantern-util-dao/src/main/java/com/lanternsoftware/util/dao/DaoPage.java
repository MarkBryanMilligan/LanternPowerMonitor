package com.lanternsoftware.util.dao;

import java.util.List;

public class DaoPage<T> {
    private List<T> results;
    private int totalResultCount;

    public DaoPage() {
    }

    public DaoPage(List<T> _results, int _totalResultCount) {
        results = _results;
        totalResultCount = _totalResultCount;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> _results) {
        results = _results;
    }

    public int getTotalResultCount() {
        return totalResultCount;
    }

    public void setTotalResultCount(int _totalResultCount) {
        totalResultCount = _totalResultCount;
    }
}
