package com.lanternsoftware.powermonitor;

import com.lanternsoftware.powermonitor.datamodel.BreakerPower;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MqttPoster {
    private static final Logger LOG = LoggerFactory.getLogger(MqttPoster.class);

    private final IMqttClient client;

    public MqttPoster(MonitorConfig _config) {
        IMqttClient c = null;
        try {
            LOG.info("Attempting to connect to MQTT broker at {}", _config.getMqttBrokerUrl());
            c = new MqttClient(_config.getMqttBrokerUrl(), String.format("Lantern_Power_Monitor_Hub_%d", _config.getHub()));
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            if (NullUtils.isNotEmpty(_config.getMqttUserName()))
                options.setUserName(_config.getMqttUserName());
            if (NullUtils.isNotEmpty(_config.getMqttPassword()))
                options.setPassword(_config.getMqttPassword().toCharArray());
            c.connect(options);
        } catch (Exception e) {
            LOG.error("Failed to create MQTT client", e);
        }
        client = c;
    }

    public void postPower(List<BreakerPower> _power) {
        for (BreakerPower power : CollectionUtils.makeNotNull(_power)) {
            String topic = "lantern_power_monitor/breaker_power/" + power.getKey();
            MqttMessage msg = new MqttMessage(NullUtils.toByteArray(DaoSerializer.toJson(power)));
            msg.setQos(2);
            msg.setRetained(true);
            try {
                client.publish(topic, msg);
            } catch (Exception e) {
                LOG.error("Failed to publish message to {}", topic, e);
            }
        }
    }
}
