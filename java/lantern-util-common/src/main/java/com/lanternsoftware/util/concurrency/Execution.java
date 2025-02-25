package com.lanternsoftware.util.concurrency;

import java.util.concurrent.Callable;

public abstract class Execution implements Callable<ExecutionResult> {
    public abstract void run() throws Exception;
    public final ExecutionResult call(){
        try {
            run();
        }
        catch (Exception _e) {
            return new ExecutionResult(_e);
        }
        return new ExecutionResult(null);
    }
}
