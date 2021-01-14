package com.lanternsoftware.util.tracing;

import java.util.Date;

public class TraceDuration {
    private long start;
    private long curStart;
    private long duration = 0;

    public TraceDuration(Date _start) {
        start = curStart = _start.getTime();
    }

    public void start() {
        curStart = new Date().getTime();
        if (duration == 0)
            start = curStart;
    }

    public void stop() {
        duration += (new Date().getTime()-curStart);
    }

    public Date currentTimeOffset() {
        return new Date(start + duration + (new Date().getTime()-curStart));
    }

    public long duration() {
        return duration;
    }

    public long end() {
        return start + duration;
    }

    public TraceContext getContext() {
        return null;
    }
}
