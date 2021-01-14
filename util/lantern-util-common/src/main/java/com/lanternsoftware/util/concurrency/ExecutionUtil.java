package com.lanternsoftware.util.concurrency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lanternsoftware.util.CollectionUtils;

public abstract class ExecutionUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionUtil.class);

    public static void waitForExecution(Collection<Future<ExecutionResult>> futures) throws Exception {
        Exception e = null;
        for (Future<ExecutionResult> f : futures) {
            ExecutionResult result = f.get();
            if (result.getException() != null)
                e = result.getException();
        }
        if (e != null)
            throw e;
    }
    
    public static void waitForExecution(Logger _logger, Collection<Future<?>> futures) {
        for (Future<?> f : futures) {
            try {
                f.get();
            }
            catch (Exception _e) {
                _logger.error("Exception occurred during execution", _e);
            }
        }
    }

    public static void waitForExecution(Future<ExecutionResult>... futures) throws Exception {
        Exception e = null;
        for (Future<ExecutionResult> f : futures) {
            ExecutionResult result = f.get();
            if (result.getException() != null)
                e = result.getException();
        }
        if (e != null)
            throw e;
    }

    public static void waitForExecution(Logger _logger, Future<?>... futures) {
        for (Future<?> f : futures) {
            try {
                f.get();
            }
            catch (Exception _e) {
                _logger.error("Exception occurred during execution", _e);
            }
        }
    }

    public static <T> T get(Future<T> _futures) {
        return CollectionUtils.getFirst(getAll(Collections.singletonList(_futures)));
    }

    public static <T> List<T> getAll(Future<T>... _futures) {
        return getAll(Arrays.asList(_futures));
    }

    public static <T> List<T> getAll(Collection<Future<T>> _futures) {
        return getAll(_futures, false);
    }

    public static <T> List<T> getAll(Collection<Future<T>> _futures, boolean _includeNulls) {
        List<T> ret = new ArrayList<>();
        for (Future<T> future : CollectionUtils.makeNotNull(_futures)) {
            try {
                T t = future.get();
                if (_includeNulls || (t != null))
                    ret.add(t);
            }
            catch (Exception e) {
                LOG.error("Exception while getting future", e);
            }
        }
        return ret;
    }
}
