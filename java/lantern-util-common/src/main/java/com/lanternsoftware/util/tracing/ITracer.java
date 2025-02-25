package com.lanternsoftware.util.tracing;

import java.util.Date;
import java.util.List;

public interface ITracer {
    void config(TracerConfig _config);
    TraceDuration createDuration(TraceContext _parent, String _name, Date _start);
    void trace(String _name, TraceDuration _duration, TraceTags _tags, List<TraceLog> _logs);
}
