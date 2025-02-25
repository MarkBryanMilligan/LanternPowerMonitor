package com.lanternsoftware.util.dao.jdbc.preparedinstatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

class InClauseBuilder {
    private final Map<Integer, InClause> inClauses = new HashMap<Integer, InClause>();
    private int maxBatchSize = 0;

    /**
     * @param _clause
     *         - {@link InClause} to be processed.
     */
    public void addClause(InClause _clause) {
        if (_clause != null) {
            inClauses.put(_clause.getStartIndex(), _clause);
        }
    }

    /**
     * Method which defines a maximum batch size used to calculate in-clause batch sizes.
     *
     * @param _batchSize
     */
    public void setMaxBatchSize(int _batchSize) {
        maxBatchSize = _batchSize;
    }

    /**
     * Method to generate a Collection of {@link InClauseStatement}s to be executed. These statements represent all possible combinations of {@link InClauseBatchedParameter}s from all {@link InClause}s that will need to be executed in order to fulfill the set in-clauses.
     *
     * @return Collection of {@link InClauseStatement}s to be executed. Will not return null, but return an empty Collection
     */
    public Collection<InClauseStatement> buildStatements() {
        Collection<InClauseStatement> collStatements = new ArrayList<InClauseStatement>();
        if (inClauses.isEmpty()) {
            return collStatements;
        }

        Map<Integer, LinkedList<InClauseBatchedParameter>> mapBatchedParameters = new HashMap<Integer, LinkedList<InClauseBatchedParameter>>();
        Iterator<Entry<Integer, InClause>> iter = inClauses.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Integer, InClause> entry = iter.next();
            if (entry == null) {
                continue;
            }

            int nStartIdx = entry.getKey();
            InClause inClause = entry.getValue();
            if (inClause == null) {
                continue;
            }

            inClause.setMaxBatchSize(maxBatchSize);
            LinkedList<InClauseBatchedParameter> batchedParameters = inClause.getBatchedParameters();
            mapBatchedParameters.put(nStartIdx, batchedParameters);
        }

        buildInClauseStatements(mapBatchedParameters, collStatements);
        return collStatements;
    }

    private void buildInClauseStatements(Map<Integer, LinkedList<InClauseBatchedParameter>> _batchedParameters, Collection<InClauseStatement> _inStatements) {
        if (_batchedParameters.isEmpty()) {
            return;
        }

        Entry<Integer, LinkedList<InClauseBatchedParameter>> entry = _batchedParameters.entrySet().iterator().next();

        int nStartIdx = entry.getKey();
        _batchedParameters.remove(nStartIdx);

        LinkedList<InClauseBatchedParameter> parameters = entry.getValue();
        if (parameters == null) {
            return;
        }

        if (_inStatements.isEmpty()) {
            for (InClauseBatchedParameter parameter : parameters) {
                if (parameter == null) {
                    continue;
                }

                InClauseStatement statement = new InClauseStatement();
                statement.setNextParameter(nStartIdx, parameter);
                _inStatements.add(statement);
            }
        }
        else {
            Collection<InClauseStatement> collNewStatements = new ArrayList<InClauseStatement>();
            Iterator<InClauseStatement> iter = _inStatements.iterator();
            while (iter.hasNext()) {
                InClauseStatement existingStatement = iter.next();
                if (existingStatement == null) {
                    continue;
                }

                // we need to clone the statement because we'll add a parameter to the first statement,
                // but we need the original to add other combinations of the parameters
                InClauseStatement clonedStatement = existingStatement.clone();

                boolean bFirst = true;
                for (InClauseBatchedParameter parameter : parameters) {
                    if (parameter == null) {
                        continue;
                    }

                    /*
                     * if there's only 1 parameter, we can just add that parameter to the existing statements for additional parameters, we
                     * need to build all of the remaining combinations for the new parameter we the existing statements
                     */
                    if (bFirst) {
                        existingStatement.setNextParameter(nStartIdx, parameter);
                        bFirst = false;
                        continue;
                    }
                    else {
                        InClauseStatement newStatement = clonedStatement.clone();
                        newStatement.setNextParameter(nStartIdx, parameter);
                        collNewStatements.add(newStatement);
                    }
                }
            }

            _inStatements.addAll(collNewStatements);
        }

        buildInClauseStatements(_batchedParameters, _inStatements);
    }

    /**
     * Method to reset the builder
     */
    public void reset() {
        for (InClause clause : inClauses.values()) {
            if (clause != null) {
                clause.reset();
            }
        }
        inClauses.clear();
        maxBatchSize = 0;
    }
}
