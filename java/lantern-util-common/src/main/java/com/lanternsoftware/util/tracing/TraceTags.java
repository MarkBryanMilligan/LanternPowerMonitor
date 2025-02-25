package com.lanternsoftware.util.tracing;

import java.util.HashMap;

public class TraceTags extends HashMap<String, String> {
    public static TraceTags tag(String _name, String _value) {
        TraceTags tags = new TraceTags();
        tags.put(_name, _value);
        return tags;
    }

    public TraceTags withTag(String _name, String _value) {
        put(_name, _value);
        return this;
    }
}
