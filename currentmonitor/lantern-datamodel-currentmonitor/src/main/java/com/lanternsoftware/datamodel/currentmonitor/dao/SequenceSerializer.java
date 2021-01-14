package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.Sequence;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class SequenceSerializer extends AbstractDaoSerializer<Sequence>
{
	@Override
	public Class<Sequence> getSupportedClass()
	{
		return Sequence.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(Sequence _o)
	{
		DaoEntity d = new DaoEntity();
		if (_o.getId() != null)
			d.put("_id", _o.getId());
		d.put("sequence", _o.getSequence());
		return d;
	}

	@Override
	public Sequence fromDaoEntity(DaoEntity _d)
	{
		Sequence o = new Sequence();
		o.setId(DaoSerializer.getString(_d, "_id"));
		o.setSequence(DaoSerializer.getInteger(_d, "sequence"));
		return o;
	}
}