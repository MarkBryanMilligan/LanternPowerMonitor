package com.lanternsoftware.currentmonitor;


import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.List;

@DBSerializable
public class MonitorConfig {
    private String host;
    private String authCode;
    private String username;
    private String password;
    private int hub;
    private boolean debug;
    private int connectTimeout;
    private int socketTimeout;
    private int updateInterval;
    private boolean autoUpdate;
    private float autoCalibrationVoltage;
    private boolean needsCalibration;
    private String mqttBrokerUrl;
    private String mqttUserName;
    private String mqttPassword;
    private double mqttVoltageCalibrationFactor;
    private double mqttPortCalibrationFactor;
    private int mqttFrequency;
    private List<Breaker> mqttBreakers;

    public MonitorConfig() {
    }

    public MonitorConfig(int _hub, String _host) {
        hub = _hub;
        host = _host;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String _host) {
        host = _host;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String _authCode) {
        authCode = _authCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String _username) {
        username = _username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String _password) {
        password = _password;
    }

    public int getHub() {
        return hub;
    }

    public void setHub(int _hub) {
        hub = _hub;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean _debug) {
        debug = _debug;
    }

    public int getConnectTimeout() {
        return connectTimeout == 0 ? 3000 : connectTimeout;
    }

    public void setConnectTimeout(int _connectTimeout) {
        connectTimeout = _connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout == 0 ? 5000 : socketTimeout;
    }

    public void setSocketTimeout(int _socketTimeout) {
        socketTimeout = _socketTimeout;
    }

    public int getUpdateInterval() {
        return updateInterval == 0 ? 300 : updateInterval;
    }

    public void setUpdateInterval(int _updateInterval) {
        updateInterval = _updateInterval;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean _autoUpdate) {
        autoUpdate = _autoUpdate;
    }

    public float getAutoCalibrationVoltage() {
        return autoCalibrationVoltage;
    }

    public void setAutoCalibrationVoltage(float _autoCalibrationVoltage) {
        autoCalibrationVoltage = _autoCalibrationVoltage;
    }

    public boolean isNeedsCalibration() {
        return needsCalibration;
    }

    public void setNeedsCalibration(boolean _needsCalibration) {
        needsCalibration = _needsCalibration;
    }

    public String getMqttBrokerUrl() {
        return mqttBrokerUrl;
    }

    public void setMqttBrokerUrl(String _mqttBrokerUrl) {
        mqttBrokerUrl = _mqttBrokerUrl;
    }

    public String getMqttUserName() {
        return mqttUserName;
    }

    public void setMqttUserName(String _mqttUserName) {
        mqttUserName = _mqttUserName;
    }

    public String getMqttPassword() {
        return mqttPassword;
    }

    public void setMqttPassword(String _mqttPassword) {
        mqttPassword = _mqttPassword;
    }

    public double getMqttVoltageCalibrationFactor() {
        return mqttVoltageCalibrationFactor;
    }

    public double getFinalVoltageCalibrationFactor() {
        if (mqttVoltageCalibrationFactor == 0.0)
            mqttVoltageCalibrationFactor = 1.0;
        return 0.3445* mqttVoltageCalibrationFactor;
    }

    public void setMqttVoltageCalibrationFactor(double _mqttVoltageCalibrationFactor) {
        mqttVoltageCalibrationFactor = _mqttVoltageCalibrationFactor;
    }

    public double getMqttPortCalibrationFactor() {
        if (mqttPortCalibrationFactor == 0.0)
            mqttPortCalibrationFactor = 1.0;
        return mqttPortCalibrationFactor;
    }

    public void setMqttPortCalibrationFactor(double _mqttPortCalibrationFactor) {
        mqttPortCalibrationFactor = _mqttPortCalibrationFactor;
    }

    public int getMqttFrequency() {
        if (mqttFrequency == 0)
            mqttFrequency = 60;
        return mqttFrequency;
    }

    public void setMqttFrequency(int _mqttFrequency) {
        mqttFrequency = _mqttFrequency;
    }

    public List<Breaker> getMqttBreakers() {
        return mqttBreakers;
    }

    public void setMqttBreakers(List<Breaker> _mqttBreakers) {
        mqttBreakers = _mqttBreakers;
    }
}
