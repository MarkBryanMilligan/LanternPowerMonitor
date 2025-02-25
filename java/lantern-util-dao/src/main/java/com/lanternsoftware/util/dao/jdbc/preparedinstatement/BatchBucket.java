package com.lanternsoftware.util.dao.jdbc.preparedinstatement;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Utility class that houses the logic to determine bucket sizes for batched in-clauses.
 */
class BatchBucket {
    private static final int MAX_BATCH_SIZE = 1000;
    private static final Collection<Integer> predefinedBucketSizes = new TreeSet<Integer>();
    static {
        predefinedBucketSizes.add(1);
        predefinedBucketSizes.add(10);
        predefinedBucketSizes.add(25);
        predefinedBucketSizes.add(50);
        predefinedBucketSizes.add(100);
        predefinedBucketSizes.add(500);
        predefinedBucketSizes.add(MAX_BATCH_SIZE);
    }

    private final Collection<Integer> bucketSizes = new TreeSet<Integer>();
    private int maxBatchSize;

    /**
     * Default Constructor. The default maximum batch size is currently 1000.
     */
    public BatchBucket() {
        bucketSizes.addAll(predefinedBucketSizes);
        maxBatchSize = MAX_BATCH_SIZE;
    }

    /**
     * Batch-size Constructor
     * 
     * @param _nMaxBatchSize
     *            - Integer specifying the maximum batch size. If this value is greater than the maximum batch size (1000),
     *            it will be truncated.
     */
    public BatchBucket(int _nMaxBatchSize) {
        if (_nMaxBatchSize <= 0 || _nMaxBatchSize > MAX_BATCH_SIZE) {
            bucketSizes.addAll(predefinedBucketSizes);
            maxBatchSize = MAX_BATCH_SIZE;
            return;
        }

        Iterator<Integer> iter = predefinedBucketSizes.iterator();
        while (iter.hasNext()) {
            int nNextBatchSize = iter.next();
            if (nNextBatchSize < _nMaxBatchSize) {
                maxBatchSize = nNextBatchSize;
                bucketSizes.add(nNextBatchSize);
            }
            else
                maxBatchSize = _nMaxBatchSize;
        }

        bucketSizes.add(_nMaxBatchSize);
    }

    /**
     * @return the calculated maximum batch size
     */
    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    /**
     * Method to calculate the appropriate batch size for a specific current size
     * 
     * @param _nCurSize
     *            - Integer representing the current size of the statement
     * @return an Integer representing the batch size
     */
    public int getBatchSize(int _nCurSize) {
        if (_nCurSize <= 0)
            return 0;

        Iterator<Integer> iter = bucketSizes.iterator();
        while (iter.hasNext()) {
            int nNextBatchSize = iter.next();
            if (_nCurSize <= nNextBatchSize)
                return nNextBatchSize;
        }

        return 0;
    }
}
