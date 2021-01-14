package com.lanternsoftware.util.tracing;

import java.util.Date;

public class TraceLog {
    private final Date timeStamp;
    private final String event;

    public TraceLog(Date _timeStamp, String _event) {
        timeStamp = _timeStamp;
        event = _event;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public String getEvent() {
        return event;
    }
}
