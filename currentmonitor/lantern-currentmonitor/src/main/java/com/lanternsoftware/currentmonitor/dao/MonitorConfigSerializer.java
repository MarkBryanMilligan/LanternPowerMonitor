package com.lanternsoftware.currentmonitor.dao;

import com.lanternsoftware.currentmonitor.MonitorConfig;
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
		d.put("update_interval", _o.getUpdateInterval());
		d.put("auto_calibration_voltage", _o.getAutoCalibrationVoltage());
		d.put("needs_calibration", _o.isNeedsCalibration());
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
		o.setUpdateInterval(DaoSerializer.getInteger(_d, "update_interval"));
		o.setAutoCalibrationVoltage(DaoSerializer.getFloat(_d, "auto_calibration_voltage"));
		o.setNeedsCalibration(DaoSerializer.getBoolean(_d, "needs_calibration"));
		return o;
	}
}