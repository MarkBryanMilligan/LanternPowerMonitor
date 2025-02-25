package com.lanternsoftware.util.tracing;

import java.util.HashMap;
import java.util.Map;

import com.lanternsoftware.util.NullUtils;

public class TraceContext extends HashMap<String, String> {
    public String serialize() {
        StringBuilder s = null;
        for (Map.Entry<String, String> e : entrySet()) {
            if (s == null)
                s = new StringBuilder();
            else
                s.append("~");
            s.append(e.getKey());
            s.append(".");
            s.append(e.getValue());
        }
        return (s == null)?"":s.toString();
    }

    public static TraceContext deserialize(String _context) {
        TraceContext context = new TraceContext();
        for (String key : NullUtils.cleanSplit(_context, "~")) {
            String[] parts = NullUtils.cleanSplit(key, "\\.");
            if (parts.length == 2)
                context.put(parts[0], parts[1]);
        }
        return context.isEmpty()?null:context;
    }
}
