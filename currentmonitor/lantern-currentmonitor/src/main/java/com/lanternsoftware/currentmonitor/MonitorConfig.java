package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.List;

@DBSerializable
public class MonitorConfig {
    private String host;
    private String authCode;
    private String username;
    private String password;
    private int hub = -1;
    private boolean debug;
    private int connectTimeout;
    private int socketTimeout;
    private boolean postSamples = false;
    private boolean needsCalibration = true;
    private boolean acceptSelfSignedCertificates = false;
    private String mqttBrokerUrl;
    private String mqttUserName;
    private String mqttPassword;
    private double mqttVoltageCalibrationFactor;
    private double mqttPortCalibrationFactor;
    private int mqttFrequency;
    private List<Breaker> mqttBreakers;
    private boolean influxDB2Enabled;
    private String influxDB2Url;
    private String influxDB2ApiToken;
    private String influxDB2Org;
    private String influxDB2Bucket;
    private String lokiUrl;

    public MonitorConfig() {
    }

    public MonitorConfig(int _hub, String _host) {
        hub = _hub;
        host = _host;
    }

    public String getHost() {
        return NullUtils.isEmpty(host) ? "https://lanternpowermonitor.com/currentmonitor/" : host;
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

    public boolean isPostSamples() {
        return postSamples;
    }

    public void setPostSamples(boolean _postSamples) {
        postSamples = _postSamples;
    }

    public boolean isNeedsCalibration() {
        return needsCalibration;
    }

    public void setNeedsCalibration(boolean _needsCalibration) {
        needsCalibration = _needsCalibration;
    }

    public boolean isAcceptSelfSignedCertificates() {
        return acceptSelfSignedCertificates;
    }

    public void setAcceptSelfSignedCertificates(boolean _acceptSelfSignedCertificates) {
        acceptSelfSignedCertificates = _acceptSelfSignedCertificates;
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
  
    public boolean getInfluxDB2Enabled() {
        return influxDB2Enabled;
    }

    public void setInfluxDB2Enabled(boolean _influxDB2Enabled) {
        influxDB2Enabled = _influxDB2Enabled;
    }

    public String getInfluxDB2Url() {
        return influxDB2Url;
    }

    public void setInfluxDB2Url(String _influxDB2Url) {
        influxDB2Url = _influxDB2Url;
    }

    public String getInfluxDB2ApiToken() {
        return influxDB2ApiToken;
    }

    public void setInfluxDB2ApiToken(String _influxDB2ApiToken) {
        influxDB2ApiToken = _influxDB2ApiToken;
    }

    public String getInfluxDB2Org() {
        return influxDB2Org;
    }

    public void setInfluxDB2Org(String _influxDB2Org) {
        influxDB2Org = _influxDB2Org;
    }

    public String getInfluxDB2Bucket() {
        return influxDB2Bucket;
    }

    public void setInfluxDB2Bucket(String _influxDB2Bucket) {
        influxDB2Bucket = _influxDB2Bucket;
    }
  
    public String getLokiUrl() {
        return NullUtils.isEmpty(lokiUrl) ? "http://127.0.0.1:3100" : lokiUrl;
    }

    public void setLokiUrl(String _lokiUrl) {
        lokiUrl = _lokiUrl;
    }
}
