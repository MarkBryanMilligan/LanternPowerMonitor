package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.datamodel.currentmonitor.BreakerPower;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttPoster {
    private static final Logger LOG = LoggerFactory.getLogger(MqttPoster.class);

    private final IMqttClient client;

    public MqttPoster(MonitorConfig _config) {
        IMqttClient c = null;
        try {
            c = new MqttClient(_config.getMqttBrokerUrl(), String.format("Lantern_Power_Monitor_Hub_%d", _config.getHub()));
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            if (NullUtils.isNotEmpty(_config.getMqttUserName()))
                options.setUserName(_config.getMqttUserName());
            if (NullUtils.isNotEmpty(_config.getMqttPassword()))
                options.setUserName(_config.getMqttPassword());
            c.connect(options);
        } catch (MqttException e) {
            LOG.error("Failed to create MQTT client", e);
        }
        client = c;
    }

    public void postPower(BreakerPower _power) {
        String topic = "lantern_power_monitor/breaker_power/" + _power.getKey();
        MqttMessage msg = new MqttMessage(NullUtils.toByteArray(DaoSerializer.toJson(_power)));
        msg.setQos(2);
        msg.setRetained(true);
        try {
            client.publish(topic, msg);
        } catch (MqttException e) {
            LOG.error("Failed to publish message to {}", topic, e);
        }
    }
}
