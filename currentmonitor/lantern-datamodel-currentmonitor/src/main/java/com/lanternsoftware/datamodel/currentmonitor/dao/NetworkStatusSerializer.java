package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.NetworkStatus;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.lang.String;
import java.util.Collections;
import java.util.List;

public class NetworkStatusSerializer extends AbstractDaoSerializer<NetworkStatus>
{
	@Override
	public Class<NetworkStatus> getSupportedClass()
	{
		return NetworkStatus.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(NetworkStatus _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("wifi_ips", _o.getWifiIPs());
		d.put("ethernet_ips", _o.getEthernetIPs());
		d.put("ping_successful", _o.isPingSuccessful());
		return d;
	}

	@Override
	public NetworkStatus fromDaoEntity(DaoEntity _d)
	{
		NetworkStatus o = new NetworkStatus();
		o.setWifiIPs(DaoSerializer.getList(_d, "wifi_ips", String.class));
		o.setEthernetIPs(DaoSerializer.getList(_d, "ethernet_ips", String.class));
		o.setPingSuccessful(DaoSerializer.getBoolean(_d, "ping_successful"));
		return o;
	}
}