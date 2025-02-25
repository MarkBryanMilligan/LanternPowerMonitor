package com.lanternsoftware.util.concurrency;

public class ExecutionResult {
    private final Exception e;

    public ExecutionResult(Exception _e) {
        e = _e;
    }

    public Exception getException() {
        return e;
    }
}
