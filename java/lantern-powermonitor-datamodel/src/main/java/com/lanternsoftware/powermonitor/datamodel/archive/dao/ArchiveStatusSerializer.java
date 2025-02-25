package com.lanternsoftware.powermonitor.datamodel.archive.dao;

import com.lanternsoftware.powermonitor.datamodel.archive.ArchiveStatus;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class ArchiveStatusSerializer extends AbstractDaoSerializer<ArchiveStatus>
{
	@Override
	public Class<ArchiveStatus> getSupportedClass()
	{
		return ArchiveStatus.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(ArchiveStatus _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("_id", _o.getId());
		d.put("account_id", _o.getAccountId());
		d.put("month", DaoSerializer.toLong(_o.getMonth()));
		d.put("progress", _o.getProgress());
		return d;
	}

	@Override
	public ArchiveStatus fromDaoEntity(DaoEntity _d)
	{
		ArchiveStatus o = new ArchiveStatus();
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setMonth(DaoSerializer.getDate(_d, "month"));
		o.setProgress(DaoSerializer.getFloat(_d, "progress"));
		return o;
	}
}