package com.lanternsoftware.currentmonitor.dao;

import com.lanternsoftware.currentmonitor.MonitorConfig;
import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class MonitorConfigSerializer extends AbstractDaoSerializer<MonitorConfig>
{
	@Override
	public Class<MonitorConfig> getSupportedClass()
	{
		return MonitorConfig.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(MonitorConfig _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("host", _o.getHost());
		d.put("auth_code", _o.getAuthCode());
		d.put("username", _o.getUsername());
		d.put("password", _o.getPassword());
		d.put("hub", _o.getHub());
		d.put("debug", _o.isDebug());
		d.put("connect_timeout", _o.getConnectTimeout());
		d.put("socket_timeout", _o.getSocketTimeout());
		d.put("post_samples", _o.isPostSamples());
		d.put("needs_calibration", _o.isNeedsCalibration());
		d.put("mqtt_broker_url", _o.getMqttBrokerUrl());
		d.put("mqtt_user_name", _o.getMqttUserName());
		d.put("mqtt_password", _o.getMqttPassword());
		d.put("mqtt_voltage_calibration_factor", _o.getMqttVoltageCalibrationFactor());
		d.put("mqtt_port_calibration_factor", _o.getMqttPortCalibrationFactor());
		d.put("mqtt_frequency", _o.getMqttFrequency());
		d.put("mqtt_breakers", DaoSerializer.toDaoEntities(_o.getMqttBreakers(), DaoProxyType.MONGO));
		return d;
	}

	@Override
	public MonitorConfig fromDaoEntity(DaoEntity _d)
	{
		MonitorConfig o = new MonitorConfig();
		o.setHost(DaoSerializer.getString(_d, "host"));
		o.setAuthCode(DaoSerializer.getString(_d, "auth_code"));
		o.setUsername(DaoSerializer.getString(_d, "username"));
		o.setPassword(DaoSerializer.getString(_d, "password"));
		o.setHub(DaoSerializer.getInteger(_d, "hub"));
		o.setDebug(DaoSerializer.getBoolean(_d, "debug"));
		o.setConnectTimeout(DaoSerializer.getInteger(_d, "connect_timeout"));
		o.setSocketTimeout(DaoSerializer.getInteger(_d, "socket_timeout"));
		o.setPostSamples(DaoSerializer.getBoolean(_d, "post_samples"));
		o.setNeedsCalibration(DaoSerializer.getBoolean(_d, "needs_calibration"));
		o.setMqttBrokerUrl(DaoSerializer.getString(_d, "mqtt_broker_url"));
		o.setMqttUserName(DaoSerializer.getString(_d, "mqtt_user_name"));
		o.setMqttPassword(DaoSerializer.getString(_d, "mqtt_password"));
		o.setMqttVoltageCalibrationFactor(DaoSerializer.getDouble(_d, "mqtt_voltage_calibration_factor"));
		o.setMqttPortCalibrationFactor(DaoSerializer.getDouble(_d, "mqtt_port_calibration_factor"));
		o.setMqttFrequency(DaoSerializer.getInteger(_d, "mqtt_frequency"));
		o.setMqttBreakers(DaoSerializer.getList(_d, "mqtt_breakers", Breaker.class));
		return o;
	}
}