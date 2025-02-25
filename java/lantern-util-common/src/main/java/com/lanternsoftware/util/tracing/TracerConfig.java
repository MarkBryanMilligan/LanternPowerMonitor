package com.lanternsoftware.util.tracing;

public class TracerConfig {
    private final String appName;
    private final String endpoint;
    private final boolean suppressLocalLog;
    private TraceFrequencyType frequencyType;
    private double frequency;
    private boolean useThreadContext = false;

    public TracerConfig(String _appName, String _endpoint) {
        this(_appName, _endpoint, true);
    }

    public TracerConfig(String _appName, String _endpoint, boolean _suppressLocalLog) {
        appName = _appName;
        endpoint = _endpoint;
        suppressLocalLog = _suppressLocalLog;
        frequencyType = TraceFrequencyType.ALL;
        frequency = 0.0;
    }

    public TracerConfig withFrequency(TraceFrequencyType _type, double _frequency) {
        frequencyType = _type;
        frequency = _frequency;
        return this;
    }

    public TracerConfig tracePercentage(double _percentage) {
        return withFrequency(TraceFrequencyType.PERCENTAGE, _percentage);
    }

    public TracerConfig traceMaximumPerSecond(int _max) {
        return withFrequency(TraceFrequencyType.MAX_TRACES_PER_SECOND, _max);
    }

    public TracerConfig traceRateControlledRemotely() {
        return withFrequency(TraceFrequencyType.REMOTE_CONTROLLED, 0.0);
    }

    public TracerConfig traceAll() {
        return withFrequency(TraceFrequencyType.ALL, 1.0);
    }

    public TracerConfig useThreadContext(boolean _useThreadContext) {
        setUseThreadContext(_useThreadContext);
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public boolean isSuppressLocalLog() {
        return suppressLocalLog;
    }

    public TraceFrequencyType getFrequencyType() {
        return frequencyType;
    }

    public double getFrequency() {
        return frequency;
    }

    public boolean isUseThreadContext() {
        return useThreadContext;
    }

    public void setUseThreadContext(boolean _useThreadContext) {
        useThreadContext = _useThreadContext;
    }
}
