package com.lanternsoftware.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import com.lanternsoftware.util.tracing.TraceLog;
import com.lanternsoftware.util.tracing.TraceTags;
import com.lanternsoftware.util.tracing.TracerConfig;
import com.lanternsoftware.util.tracing.ITracer;
import com.lanternsoftware.util.tracing.TraceContext;
import com.lanternsoftware.util.tracing.TraceDuration;
import org.slf4j.Logger;

public class DebugTimer {
    private final Logger LOG;
    private final String name;
    private final TraceContext context;
    private Date start;
    private TraceDuration duration;
    private TraceTags tags;
    private List<TraceLog> logs;
    private final boolean suppressLocalLogs;

    private static ITracer tracer = null;
    private static TracerConfig config;
    private static final ThreadLocal<TraceContext> traceContexts = new ThreadLocal<>();

    public DebugTimer(String _name) {
        this(null, _name, null, false);
    }

    public DebugTimer(TraceContext _parent, String _name) {
        this(_parent, _name, null, false);
    }

    public DebugTimer(String _name, Logger _log) {
        this(null, _name, _log, false);
    }

    public DebugTimer(String _context, String _name) {
        this(TraceContext.deserialize(_context), _name, null, false);
    }

    public DebugTimer(String _context, String _name, Logger _log) {
        this(TraceContext.deserialize(_context), _name, _log, false);
    }

    public DebugTimer(TraceContext _parent, String _name, Logger _log) {
        this(_parent, _name, _log, false);
    }

    public DebugTimer(TraceContext _parent, String _name, Logger _log, boolean _suppressLocalLogs) {
        TraceContext parent = _parent;
        name = _name;
        LOG = _log;
        suppressLocalLogs = _suppressLocalLogs;
        start = new Date();
        if ((parent == null) && (config != null) && config.isUseThreadContext()) {
            parent = traceContexts.get();
            traceContexts.set(getContext());
        }
        context = parent;
    }

    public static void setTracerConfig(TracerConfig _config) {
        config = _config;
        if (tracer == null) {
            Iterator<ITracer> iter =  ServiceLoader.load(ITracer.class).iterator();
            if (iter.hasNext()) {
                tracer = iter.next();
            }
        }
        tracer.config(config);
    }

    private ITracer getTracer() {
        if (config == null)
            return null;
        return tracer;
    }

    public void tag(String _name, String _value) {
        if (tags == null)
            tags = TraceTags.tag(_name, _value);
        else
            tags.put(_name, _value);
    }

    public void log(String _event) {
        if (logs == null)
            logs = new ArrayList<>();
        logs.add(new TraceLog(traceDuration().currentTimeOffset(), _event));
    }

    private TraceDuration traceDuration() {
        if (duration != null)
            return duration;
        ITracer t = getTracer();
        duration = (t == null)?new TraceDuration(start):t.createDuration(context, name, start);
        return duration;
    }

    public void start() {
        if (duration == null)
            start = new Date();
        traceDuration().start();
    }

    public TraceContext stop() {
        traceDuration().stop();
        return print();
    }

    public void stopDoNotPrint() {
        traceDuration().stop();
    }

    public long duration() {
        return traceDuration().duration();
    }

    public TraceContext getContext() {
        return traceDuration().getContext();
    }

    public TraceContext print() {
        StringBuilder b = new StringBuilder(name);
        b.append(": ");
        b.append(traceDuration().duration());
        b.append("ms");
        TraceContext newContext = traceDuration().getContext();
        ITracer t = getTracer();
        if (t != null)
            t.trace(name, duration, tags, logs);
        if (!suppressLocalLogs && ((config == null) || !config.isSuppressLocalLog())) {
            if (LOG != null)
                LOG.debug(b.toString());
            else
                System.out.println(b.toString());
        }
        traceContexts.set(context);
        return newContext;
    }
}