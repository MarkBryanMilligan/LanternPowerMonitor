package com.lanternsoftware.util.concurrency;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class ExecutorUtil {
    public static ThreadPoolExecutor fixedThreadPool(int _threadCount) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(_threadCount, _threadCount,60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }
}
